(ns health-samurai-task.integration-test
  (:require [clojure.test :refer :all]
            [health-samurai-task.crud :as crud]
            [health-samurai-task.rpc :as rpc]
            [health-samurai-task.db :as db]
            [clojure.data.json :as json])
  (:import (java.io ByteArrayInputStream)))

(defn- get-var [var default]
  (if-let [env-var (not-empty (System/getenv var))]
    env-var
    default))

(def state {:user     "crud_user"
            :password "password"
            :dbname   "crud_test"
            :host     (get-var "DB_HOST" "localhost")
            :port     (Long/parseLong (get-var "DB_PORT" "5432"))})

(defn- fix-truncate-table [t]
  (db/truncate state)
  (t))

(use-fixtures :once fix-truncate-table)

(defn- string->stream [s]
  (-> s
      (.getBytes "UTF-8")
      (ByteArrayInputStream.)))

(defn- wrap-request [method body]
  (let [b (-> {:method method :params body} json/write-str string->stream)]
    {:uri            "/rpc"
     :request-method "post"
     :body           b}))

(defn- wrap-response [body]
  (rpc/do-result body))

(defn- request-genders []
  (rpc/app state (wrap-request "list-genders" {})))

(defn- request-creation [patient]
  (rpc/app state (wrap-request "create" patient)))

(defn- request-search [filter]
  (rpc/app state (wrap-request "list" filter)))

(defn- request-update [patch]
  (rpc/app state (wrap-request "update" patch)))

(defn- request-deletion [id]
  (rpc/app state (wrap-request "delete" {:insurance_id id})))

(defn- assert-db-state
  ([expected]
   (assert-db-state expected {}))
  ([expected filter]
   (let [state (into #{} (db/select state filter))]
     (is (= state expected)))))

(deftest get-genders-enum-test
  (testing "rpc/list-genders"
    (let [result (request-genders)
          expected (wrap-response crud/genders)]
      (is (= result expected)))))

(def patient-a {:name         "A1"
                :lastname     "A2"
                :patronymic   "A3"
                :birthday     "1990-01-01"
                :gender       "male"
                :address      "A4"
                :insurance_id 11})

(def patient-b {:name         "B1"
                :lastname     "B2"
                :patronymic   "B3"
                :birthday     "1992-02-02"
                :gender       "female"
                :address      "B4"
                :insurance_id 12})

(def update-fields {:name         "C1"
                    :lastname     "C2"
                    :patronymic   "C3"
                    :gender       "transgender-female"
                    :insurance_id 11})

(def updated-patient-a (merge patient-a update-fields))

(def search-fields {:name       "A"
                    :lastname   "A"
                    :patronymic "A"
                    :address    "A"})

(defn- assert-rpc-response [actual expected]
  (is (= actual (wrap-response expected))))

(deftest test-crud
  (testing "rpc/create"
    (assert-rpc-response (request-creation patient-a) crud/create-response)
    (assert-rpc-response (request-creation patient-b) crud/create-response)
    (assert-db-state #{patient-a patient-b}))
  (testing "rpc/search"
    (assert-rpc-response (request-search {}) [patient-a patient-b])
    (assert-rpc-response (request-search {:insurance_id 1}) [])
    (assert-rpc-response (request-search {:insurance_id 11}) [patient-a])
    (doseq [[k v] search-fields]
      (assert-rpc-response (request-search {k v}) [patient-a])))
  (testing "rpc/update"
    (assert-rpc-response (request-update update-fields) crud/update-response)
    (assert-db-state #{updated-patient-a patient-b}))
  (testing "rpc/delete"
    (assert-rpc-response (request-deletion 12) crud/delete-response)
    (assert-db-state #{updated-patient-a})
    (assert-rpc-response (request-deletion 11) crud/delete-response)
    (assert-db-state #{})))
