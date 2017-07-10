(defproject sparta-calendar-lambda "0.1.0-SNAPSHOT"
  :description "sparta-calendar backend AWS lambda function"
  :url "https://github.com/mveith/sparta-calendar-lambda"
  :dependencies [[org.clojure/clojure       "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [org.clojure/core.async    "0.2.395"]
                 [io.nervous/cljs-lambda    "0.3.5"]
                 [io.nervous/eulalie "0.6.10"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-npm       "0.6.0"]
            [io.nervous/lein-cljs-lambda "0.6.6"]]
  :npm {:dependencies [
    [source-map-support "0.4.0"]]}
  :source-paths ["src"]
  :cljs-lambda
  {:defaults      {:role ""}
   :resource-dirs ["static"]
   :functions
   [{:name   "matches"
     :invoke sparta-calendar-lambda.core/matches}]}
  :cljsbuild
  {:builds [{:id "sparta-calendar-lambda"
             :source-paths ["src"]
             :compiler {:output-to     "target/sparta-calendar-lambda/sparta_calendar_lambda.js"
                        :output-dir    "target/sparta-calendar-lambda"
                        :source-map    true
                        :target        :nodejs
                        :language-in   :ecmascript5
                        :optimizations :none}}]})
