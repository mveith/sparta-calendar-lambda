(ns sparta-calendar-lambda.core
  (:require [cljs-lambda.macros :refer-macros [defgateway]]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [<!]]
            [eulalie.platform :as eul]
            [clojure.string :as str]
            [cljs-time.core :as time])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def matches-url "http://www.sparta.cz/srv/www/cs/football/match/viewNextMatches.do")

(defn get [url]
   (go (let [{:keys [error body status] :as response} (<! (eul/http-get! url))]
     body)))

(defn get-matches-table [html]
  (->
    (str/split html #"<div class=\"tableContainer\">")
    (second)
    (str/split #"<table>")
    (str/split #"</table>")
    (first)))

(defn get-matches-rows [table]
  (drop 2 (str/split table #"<tr>")))

(defn get-match-cells [row]
  (str/split row #"\\n"))

(defn get-td-content [td]
  (->
    (str/split td #"<td>")
    (second)
    (str/split #"</td>")
    (first)
    (str/split #">")
    (last)
    (str/split #"<")
    (first)))

(defn parse-date [date-time-string]
  (let [parts (filter (fn [s] (not (str/blank? s))) (str/split date-time-string #"[. |:]"))]
    (let [date-time (time/date-time (int (nth parts 2)) (int (nth parts 1)) (int (nth parts 0)) (int (nth parts 3)) (int (nth parts 4)))]
      {
       :day         (time/day date-time)
       :month       (time/month date-time)
       :year        (time/year date-time)
       :hour        (time/hour date-time)
       :minute      (time/minute date-time)
       :day-of-week (time/day-of-week date-time)
       })))

(defn starts-with? [string pattern]
  (and
    (>= (count string) (count pattern))
    (= (subs string 0 (count pattern)) pattern)))

(defn home-match? [home-team-name]
  (starts-with? home-team-name "AC Sparta Praha"))

(defn create-match [row]
  (let [cells (get-match-cells row)]
    {
     :team          (get-td-content (nth cells 1))
     :event         (get-td-content (nth cells 3))
     :home-team     (get-td-content (nth cells 5))
     :away-team     (get-td-content (nth cells 9))
     :date          (parse-date (get-td-content (nth cells 10)))
     :is-home-match (home-match? (get-td-content (nth cells 5)))
     }))

(defn get-matches [html]
  (map create-match
    (->
      (get-matches-table html)
      (get-matches-rows)
      )))

(def aws-sdk
  (nodejs/require "aws-sdk"))
(def s3 (new aws-sdk.S3))
(def bucketName "sparta-calendar.mveith.com")
(def fileName "data.js")

(defn get-bucket [data] (js-obj "Bucket" bucketName "Key" fileName "Body" data))

(defn done [err data]   
  (js/console.log "odesláno:")
  (js/console.log err)
  (js/console.log data))

(defn send [data] 
  (.putObject s3 (get-bucket data) done))
        
(defn prepare-data [data]
  (let [escaped (str/replace data "\"" "\\\"")
        trimmed (subs escaped 1 (- (count escaped) 1))]
     (str "matchesData = \"[" trimmed "]\"")))

(def ^:export matches
  (async-lambda-fn
   (fn [event ctx]
     (go 
       (let [html (<! (get matches-url))]
        (send (prepare-data (str (get-matches html))))
        {
          :body (str (get-matches html))
          :status 200 })))))