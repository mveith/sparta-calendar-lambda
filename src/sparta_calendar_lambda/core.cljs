(ns sparta-calendar-lambda.core
  (:require [cljs-lambda.macros :refer-macros [defgateway]]))
 
(defgateway matches
  [& args]
  {:status 200
   :body "{\"hello\": \"world\"}"})