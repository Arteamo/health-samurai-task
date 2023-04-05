(ns frontend.fields
  (:require [frontend.client :as client]
            [frontend.util :as util]))

(defn specify-params [type params]
  (if (= type "date")
    (assoc params :max "9999-01-01")
    params))

(defn input-field [form-atom id label type required on-change]
  (let [id-str (name id)
        params {:type      type
                :id        id-str
                :required  required
                :value     (id @form-atom)
                :on-change (fn [e] (on-change form-atom id e))}]
    [:div.mb-3
     [:label.form-label {:for id-str} label]
     [:input.form-control (specify-params type params)]]))

(defn on-creation-change [form-atom id e]
  (swap! form-atom assoc id (-> e .-target .-value)))

(defn creation-input-field [form-atom id label type]
  (input-field form-atom id label type true on-creation-change))

(defn search-on-change [form-atom id e]
  (if-let [param (-> e .-target .-value not-empty)]
    (swap! form-atom assoc id param)
    (swap! form-atom dissoc id))
  (client/get-patients))

(defn search-input-field [form-atom id label type]
  (input-field form-atom id label type false search-on-change))

(defn edit-on-change [form-atom id e]
  (if-let [param (-> e .-target .-value not-empty)]
    (swap! form-atom assoc id param)
    (swap! form-atom dissoc id)))

(defn edit-input-field [form-atom id label type]
  (input-field form-atom id label type false edit-on-change))

(defn gender-field [form-atom]
  [:div.mb-3
   [:label.form-label {:for "gender"} "Gender"]
   [:select.form-select {:id        "gender"
                         :value     (:gender @form-atom)
                         :on-change (fn [e]
                                      (swap! form-atom assoc-in [:gender] (-> e .-target .-value)))}
    (doall
      (let [genders (:result @client/genders-atom)]
        (for [gender genders]
          [:option {:key gender :value gender} (util/gender-view gender)])))]])