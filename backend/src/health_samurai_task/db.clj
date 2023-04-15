(ns health-samurai-task.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str])
  (:import (java.sql Date)))

(def db-static-params {:dbtype     "postgresql"
                       :ssl        false
                       :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

(defn- pg-db [state]
  (merge db-static-params (:db state)))

(extend-protocol jdbc/IResultSetReadColumn
  Date
  (result-set-read-column [v _ _]
    (.toString (.toLocalDate v))))

(def case-insensitive-like "ILIKE")
(def eq "=")
(def string-search
  {:name       case-insensitive-like
   :lastname   case-insensitive-like
   :patronymic case-insensitive-like
   :address    case-insensitive-like})

(defn- get-operation [kv]
  (let [[k _] kv]
    (str (name k) " " (get string-search k eq) " ?")))

(defn insert [state entity]
  (jdbc/insert! (pg-db state) :patient entity))

(defn- build-clauses [filter]
  (str/join " AND " (into [] (for [kv filter]
                               (get-operation kv)))))

(defn- transform-filter [filter]
  (if-let [clauses (not-empty (build-clauses filter))]
    (str " WHERE " clauses)
    ""))

(defn build-query [filter]
  (str "SELECT * FROM patient" (transform-filter filter)))

(defn extract-values [filter]
  (for [kv filter]
    (if (contains? string-search (first kv))
      (str "%" (second kv) "%")
      (second kv))))

(defn select [state filter]
  (jdbc/query (pg-db state)
              (cons (build-query filter)
                    (extract-values filter))))

(defn update [state entity]
  (jdbc/update! (pg-db state) :patient entity ["insurance_id = ?" (:insurance_id entity)]))

(defn delete [state id]
  (jdbc/delete! (pg-db state) :patient ["insurance_id = ?" id]))

(defn truncate [state]
  (jdbc/db-do-commands (pg-db state) "TRUNCATE patient"))
