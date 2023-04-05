(ns frontend.client
  (:require
    [ajax.core :refer [ajax-request json-request-format json-response-format]]
    [reagent.core :as r]
    [frontend.util :as config]))

(def patients-atom (r/atom nil))
(def genders-atom (r/atom nil))
(def search-atom (r/atom {}))

(defn transform-req [method data]
  {:method method
   :params data})

(defn do-req [data handler]
  (ajax-request
    {:uri             (str config/backend "/rpc")
     :method          :post
     :params          data
     :handler         handler
     :format          (json-request-format)
     :response-format (json-response-format {:keywords? true})}))

(defn alert [response]
  (println response)
  (js/alert (-> response :response :error)))

(defn patients-handler [[ok response]]
  (if ok
    (reset! patients-atom response)
    (alert response)))

(defn get-patients []
  (do-req (transform-req "list" @search-atom) patients-handler))

(defn reload-handler [[ok response]]
  (if ok
    (get-patients)
    (alert response)))

(defn delete-patient [insurance-id]
  (do-req (transform-req "delete" {:insurance_id insurance-id}) reload-handler))

(defn create-patient [patient]
  (do-req (transform-req "create" patient) reload-handler))

(defn update-patient [patch]
  (do-req (transform-req "update" patch) reload-handler))

(defn genders-handler [[ok response]]
  (if ok
    (reset! genders-atom response)
    (.error js/console response)))

(defn get-genders []
  (do-req (transform-req "list-genders" {}) genders-handler))
