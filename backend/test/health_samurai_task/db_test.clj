(ns health-samurai-task.db-test
  (:require [clojure.test :refer :all]
            [health-samurai-task.db :as db]))

(def no-filter-result "SELECT * FROM patient")
(def full-filter-result "SELECT * FROM patient WHERE name ILIKE ? AND lastname ILIKE ? AND patronymic ILIKE ? AND gender = ? AND address ILIKE ? AND insurance_id = ?")

(deftest test-select-query-building
  (testing "with no filter"
    (db/build-query {})
    (is (= (db/build-query {}) no-filter-result)))
  (testing "with full filter"
    (let [query (db/build-query {
                                 :name         "Name"
                                 :lastname     "Lastname"
                                 :patronymic   "Patronymic"
                                 :gender       "male"
                                 :address      "address"
                                 :insurance_id 1000})]
      (is (= query full-filter-result)))))
