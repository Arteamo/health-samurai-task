(ns health-samurai-task.crud
  (:require [clojure.spec.alpha :as s]
            [health-samurai-task.db :as db]
            [health-samurai-task.common :refer [validate-or-throw] :as common]))

(def genders #{"transgender-female"                         ; https://www.hl7.org/fhiR/codesystem-gender-identity.html
               "transgender-male"
               "non-binary"
               "male"
               "female"
               "other"
               "non-disclose"})

(s/def :params/name ::common/ne-string)
(s/def :params/lastname ::common/ne-string)
(s/def :params/patronymic (s/nilable string?))
(s/def :params/birthday ::common/->date)
(s/def :params/gender #(contains? genders %))
(s/def :params/address ::common/ne-string)
(s/def :params/insurance_id ::common/->long)

(s/def ::create
  (s/keys :req-un [:params/name
                   :params/lastname
                   :params/patronymic
                   :params/birthday
                   :params/gender
                   :params/address
                   :params/insurance_id]))

(s/def ::search
  (s/keys :opt-un [:params/name
                   :params/lastname
                   :params/patronymic
                   :params/insurance_id]))

(s/def ::update
  (s/keys :opt-un [:params/name
                   :params/lastname
                   :params/patronymic
                   :params/gender
                   :params/address]
          :req-un [:params/insurance_id]))

(s/def ::delete
  (s/keys :req-un [:params/insurance_id]))

(def create-response {:status "CREATED"})

(defn create [params]
  (db/insert (validate-or-throw ::create params))
  create-response)

(defn search [params]
  (db/select (validate-or-throw ::search params)))

(def update-response {:status "UPDATED"})

(defn update [params]
  (db/update (validate-or-throw ::update params))
  update-response)

(def delete-response {:status "DELETED"})
(defn delete [params]
  (db/delete (:insurance_id (validate-or-throw ::delete params)))
  delete-response)
