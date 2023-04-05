(ns health-samurai-task.common
  (:require [clojure.spec.alpha :as s])
  (:import [java.time LocalDate]))

(defn with-conformer [f]
  (s/conformer
    (fn [value]
      (try
        (f value)
        (catch Exception _ ::s/invalid)))))

(s/def ::ne-string
  (fn [val]
    (and (string? val)
         (not (empty? val)))))

(s/def ::->date
  (s/and
    ::ne-string
    (with-conformer #(LocalDate/parse %))))

(s/def ::->long
  (with-conformer #(Long/parseLong (str %))))


(defn validate [spec payload]
  (s/conform spec payload))

(defn validate-or-throw [spec payload]
  (let [result (validate spec payload)]
    (if (= result ::s/invalid)
      (throw (ex-info "Invalid payload" {:explanation (s/explain-str spec payload)}))
      result)))
