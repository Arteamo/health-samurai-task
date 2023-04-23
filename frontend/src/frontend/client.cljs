(ns frontend.client
  (:require
    [frontend.util :as util]
    [re-frame.core :as rf]
    [ajax.core :refer [ajax-request json-request-format json-response-format] :as ajax]))

(defn- transform-req [method data]
  {:method method
   :params data})

(defn build-req [on-success method data]
  {:method          :post
   :uri             util/backend
   :params          (transform-req method data)
   :format          (ajax/json-request-format)
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [on-success]
   :on-failure      [::process-error]})

(rf/reg-event-fx
  ::process-error
  (fn [_ [_ response]]
    (js/alert (:error response))
    {}))
