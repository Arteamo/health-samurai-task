(ns frontend.event
  (:require [frontend.util :as util]
            [frontend.client :as client]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]))

;; init
(rf/reg-event-fx
  ::initialize
  (fn [_ _]
    {:db {:patients []
          :search   {}
          :genders  []
          :creation util/default-creation-data
          :patch    {}}
     :fx [[:dispatch [::get-patients]]
          [:dispatch [::get-genders]]]}))

;; events
(rf/reg-event-fx
  ::get-genders
  (fn [_ _]
    {:http-xhrio (client/build-req ::process-genders "list-genders" {})}))

(rf/reg-event-db
  ::process-genders
  (fn [db [_ response]]
    (assoc db :genders (:result response))))

(rf/reg-event-fx
  ::get-patients
  (fn [{db :db} _]
    {:db         db
     :http-xhrio (client/build-req ::save-patients "list" (:search db))}))

(rf/reg-event-fx
  ::update-search
  (fn [{db :db} [_ field value]]
    {:db (if (not-empty value)
           (assoc-in db [:search field] value)
           (update-in db [:search] dissoc field))
     :fx [[:dispatch [::get-patients]]]}))

(rf/reg-event-db
  ::save-patients
  (fn [db [_ response]]
    (assoc db :patients (:result response))))

(rf/reg-event-fx
  ::delete-patient
  (fn [_ [_ insurance-id]]
    {:http-xhrio (client/build-req ::get-patients "delete" {:insurance_id insurance-id})}))

(rf/reg-event-db
  ::update-creation
  (fn [db [_ field value]]
    (assoc-in db [:creation field] value)))

(rf/reg-event-fx
  ::create-patient
  (fn [{db :db} _]
    {:http-xhrio (client/build-req ::get-patients "create" (:creation db))
     :db         (assoc db :creation util/default-creation-data)}))

(rf/reg-event-db
  ::update-patch
  (fn [db [_ field value]]
    (if (not-empty value)
      (assoc-in db [:patch field] value)
      (update-in db [:patch] dissoc field))))

(rf/reg-event-fx
  ::update-patient
  (fn [{db :db} [_ patient]]
    (let [id (select-keys patient [:insurance_id])
          patch (:patch db)
          result-payload (merge id patch)]
      {:http-xhrio (client/build-req ::get-patients "update" result-payload)
       :db         (assoc db :patch {})})))

;; queries
(rf/reg-sub
  ::query-genders
  (fn [db _] (:genders db)))

(rf/reg-sub
  ::query-patients
  (fn [db _] (:patients db)))

(rf/reg-sub
  ::query-search
  (fn [db _] (:search db)))

(rf/reg-sub
  ::query-creation
  (fn [db _] (:creation db)))

(rf/reg-sub
  ::query-patch
  (fn [db [_ patient]] (merge patient (:patch db))))
