(ns frontend.fields
  (:require [frontend.util :as util]
            [frontend.event :as e]
            [re-frame.core :as rf]))

(defn- specify-params [type params]
  (if (= type "date")
    (assoc params :max "9999-01-01")
    params))

(defn- input-field [form id label type required on-change]
  (let [id-str (name id)
        params {:type      type
                :id        id-str
                :required  required
                :value     (id @form)
                :on-change (fn [e] (on-change id e))}]
    [:div.mb-3
     [:label.form-label {:for id-str} label]
     [:input.form-control (specify-params type params)]]))

(defn- dispatch [event id e]
  (rf/dispatch [event id (-> e .-target .-value)]))

(defn- on-change-creation [id e]
  (dispatch ::e/update-creation id e))

(defn creation-input-field [form id label type required]
  (input-field form id label type required on-change-creation))

(defn- on-change-search [id e]
  (dispatch ::e/update-search id e))

(defn search-input-field [form id label type]
  (input-field form id label type false on-change-search))

(defn- on-change-edit [id e]
  (dispatch ::e/update-patch id e))

(defn edit-input-field [edit-form id label type]
  (input-field edit-form id label type false on-change-edit))

(defn- gender-field [form-data on-change]
  [:div.mb-3
   [:label.form-label {:for "gender"} "Gender"]
   [:select.form-select {:id        "gender"
                         :value     (:gender @form-data)
                         :on-change (fn [e] (on-change :gender e))}
    (doall
      (let [genders @(rf/subscribe [::e/query-genders])]
        (for [gender genders]
          [:option {:key gender :value gender} (util/gender-view gender)])))]])

(defn creation-gender-field [form-data]
  (gender-field form-data on-change-creation))

(defn edit-gender-field [form-data]
  (gender-field form-data on-change-edit))
