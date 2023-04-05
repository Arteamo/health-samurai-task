(ns frontend.util
  (:require [clojure.string :as str]))

(def backend "http://localhost:8080")
(def default-gender "non-disclose")

(defn gender-view [gender-id]
  (str/capitalize (str/replace gender-id "-" " ")))
