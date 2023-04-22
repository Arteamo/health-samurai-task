(ns health-samurai-task.db
  (:require [clojure.java.jdbc :as jdbc]
            [honey.sql :as sql])
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

(defn- build-string-search [k v]
  [:ilike k (str "%" v "%")])

(defn- build-eq-search [k v]
  [:= k v])

(def search-map
  {:name         build-string-search
   :lastname     build-string-search
   :patronymic   build-string-search
   :insurance_id build-eq-search})

(defn- build-clauses [filter]
  (for [[k v] filter]
    ((get search-map k) k v)))

(defn- transform-filter [filter]
  (if-let [clauses (not-empty (build-clauses filter))]
    {:where (cons :and clauses)}
    {}))

(defn build-query [filter]
  (sql/format (merge {:select :* :from :patient} (transform-filter filter))))

(defn select [state filter]
  (jdbc/query (pg-db state) (build-query filter)))

(defn- execute [state data]
  (jdbc/execute! (pg-db state) (sql/format data)))

(defn insert [state entity]
  (execute state {:insert-into :patient
                  :values      [entity]}))

(defn update [state entity]
  (execute state {:update :patient
                  :set    (dissoc entity :insurance_id)
                  :where  [:= :insurance_id (:insurance_id entity)]}))

(defn delete [state id]
  (execute state {:delete-from :patient
                  :where       [:= :insurance_id id]}))

(defn truncate [state]
  (execute state {:truncate :patient}))
