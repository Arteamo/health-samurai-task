(ns health-samurai-task.db-test
  (:require [clojure.test :refer :all]
            [health-samurai-task.db :as db]))

(def no-filter-result ["SELECT * FROM patient"])
(def full-filter-result ["SELECT * FROM patient WHERE (name ILIKE ?) AND (lastname ILIKE ?) AND (patronymic ILIKE ?) AND (insurance_id = ?)"
                         "%Name%"
                         "%Lastname%"
                         "%Patronymic%"
                         1000])

(deftest test-select-query-building
  (testing "with no filter"
    (is (= (db/build-query {}) no-filter-result)))
  (testing "with full filter"
    (let [query (db/build-query {:name         "Name"
                                 :lastname     "Lastname"
                                 :patronymic   "Patronymic"
                                 :insurance_id 1000})]
      (is (= query full-filter-result)))))
