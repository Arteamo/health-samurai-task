(ns frontend.util
  (:require [clojure.string :as str]))

(def backend "http://localhost:8080/rpc")
(def default-gender "non-disclose")

(def default-creation-data {:name         ""
                            :lastname     ""
                            :patronymic   ""
                            :gender       default-gender
                            :address      ""
                            :birthday     ""
                            :insurance_id ""})

(defn gender-view [gender-id]
  (str/capitalize (str/replace gender-id "-" " ")))
