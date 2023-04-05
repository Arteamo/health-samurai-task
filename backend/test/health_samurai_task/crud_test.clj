(ns health-samurai-task.crud-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [health-samurai-task.crud :as crud]
            [health-samurai-task.common :as common]
            [clojure.math.combinatorics :as combo]))

(def valid "valid params")
(def invalid "invalid params")

(defn is-invalid [spec payload]
  (= (common/validate spec payload) ::s/invalid))

(defn should-be-valid
  ([spec payload expected]
   (is (= (common/validate spec payload) expected)))
  ([spec payload]
   (is (not (is-invalid spec payload)))))

(defn should-be-invalid [spec payload]
  (is (is-invalid spec payload)))

(deftest test-deletion-validation
  (testing valid
    (should-be-valid ::crud/delete {:insurance_id 1000} {:insurance_id 1000})
    (should-be-valid ::crud/delete {:insurance_id "1000"} {:insurance_id 1000}))
  (testing invalid
    (should-be-invalid ::crud/delete {:insurance_id nil})
    (should-be-invalid ::crud/delete {:insurance_id 1/2})
    (should-be-invalid ::crud/delete {:insurance_id ""})))

(defn test-optional-fields
  ([spec fields f]
   (let [combos (combo/combinations fields (- (count fields) 1))]
     (doseq [combo combos]
       (f spec (into {} combo)))))
  ([spec fields k v f]
   (let [combos (combo/combinations fields (- (count fields) 1))]
     (doseq [combo combos]
       (f spec (assoc (into {} combo) k v))))))

(def update-fields {:name       "a"
                    :lastname   "b"
                    :patronymic "c"
                    :address    "addr"
                    :gender     "male"})
(deftest test-update-validation
  (testing valid
    (test-optional-fields ::crud/update update-fields :insurance_id 1000 should-be-valid)
    (should-be-valid ::crud/update {:insurance_id 1000}))
  (testing invalid
    (should-be-invalid ::crud/update {:name "name"})
    (should-be-invalid ::crud/update {:insurance_id 1000 :gender "invalid-enum-value"})))

(def search-fields {:name         "a"
                    :lastname     "b"
                    :patronymic   "c"
                    :insurance_id 1000})
(deftest test-search-validation
  (testing valid
    (should-be-valid ::crud/search {})
    (test-optional-fields ::crud/search search-fields should-be-valid)))

(def create-fields {:name         "a"
                    :lastname     "b"
                    :patronymic   "c"
                    :address      "addr"
                    :gender       "male"
                    :insurance_id 1000
                    :birthday     "1990-01-01"})

(deftest test-creation-validation
  (testing valid
    (should-be-valid ::crud/create create-fields))
  (testing invalid
    (test-optional-fields ::crud/create create-fields should-be-invalid)
    (should-be-invalid ::crud/create (assoc create-fields :birthday "illegal-date-format"))))
