(ns sparta-calendar-lambda.core
  (:require [cljs-lambda.macros :refer-macros [defgateway]]
            [cljs-lambda.util :refer [async-lambda-fn]]
            [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [<!]]
            [eulalie.platform :as eul])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn get [url]
   (go (let [{:keys [error body status] :as response} (<! (eul/http-get! url))]
     body)))

(def ^:export matches
  (async-lambda-fn
   (fn [event ctx]
     (go 
       (let [body (<! (get "http://example.com/"))]
      {:body body
      :status 200})))))