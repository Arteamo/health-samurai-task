(ns frontend.core
  (:require [reagent.core :as r]
            [frontend.util :as config]
            [frontend.fields :as field]
            [frontend.event :as e]
            [re-frame.core :as rf]))

(defn submit-creation [e]
  (.preventDefault e)
  (rf/dispatch [::e/create-patient]))

(defn create-view []
  (let [patient (rf/subscribe [::e/query-creation])]
    [(fn []
       [:div.dropdown
        [:button.btn.btn-primary.dropdown-toggle
         {:data-bs-toggle "dropdown" :aria-expanded "false" :data-bs-auto-close "outside"} "Create new"]
        [:form.dropdown-menu.p-4 {:on-submit (fn [e] (submit-creation e))}
         (field/creation-input-field patient :name "First name" "text" true)
         (field/creation-input-field patient :lastname "Last name" "text" true)
         (field/creation-input-field patient :patronymic "Patronymic" "text" false)
         (field/creation-gender-field patient)
         (field/creation-input-field patient :address "Address" "text" true)
         (field/creation-input-field patient :birthday "Birthday" "date" true) ;; todo: fix date setting
         (field/creation-input-field patient :insurance_id "Insurance id" "number" true)
         [:input.btn.btn-primary {:type "submit" :value "Create"}]]])]))

(defn submit-edit [patient e]
  (.preventDefault e)
  (rf/dispatch [::e/update-patient patient]))

(defn edit-view [patient]
  (let [edit-form (rf/subscribe [::e/query-patch patient])]
    [(fn []
       [:div.dropdown {:key (:insurance_id patient)}
        [:button.btn.btn-primary.dropdown-toggle
         {:data-bs-toggle "dropdown" :aria-expanded "false" :data-bs-auto-close "outside"} "Edit"]
        [:form.dropdown-menu.p-4 {:on-submit (fn [e] (submit-edit patient e))}
         (field/edit-input-field edit-form :name "First name" "text")
         (field/edit-input-field edit-form :lastname "Last name" "text")
         (field/edit-input-field edit-form :patronymic "Patronymic" "text")
         (field/edit-gender-field edit-form)
         (field/edit-input-field edit-form :address "Address" "text")
         [:input.btn.btn-primary {:type "submit" :value "Save"}]]])]))

(defn patient-view [patient]
  (let [insurance-id (:insurance_id patient)]
    [:tr {:key (:insurance_id patient)}
     [:td (:name patient)]
     [:td (:lastname patient)]
     [:td (:patronymic patient)]
     [:td (-> patient :gender config/gender-view)]
     [:td (:birthday patient)]
     [:td (:address patient)]
     [:td insurance-id]
     [:td [:button.btn.btn-danger {:onClick (fn [] (rf/dispatch [::e/delete-patient insurance-id]))} "Delete"]]
     [:td (edit-view patient)]]))

(defn patients-view []
  [:table.table
   [:thead
    [:tr [:th "Name"]
     [:th "Last name"]
     [:th "Patronymic"]
     [:th "Gender"]
     [:th "Birthday"]
     [:th "Address"]
     [:th "Insurance id"]
     [:th {:colSpan 2} "Actions"]]]
   [:tbody
    (doall
      (let [patients @(rf/subscribe [::e/query-patients])]
        (for [patient patients]
          (patient-view patient))))]])

(defn search-view []
  (let [search (rf/subscribe [::e/query-search])]
    [:div.dropdown
     [:button.btn.btn-primary.dropdown-toggle
      {:data-bs-toggle "dropdown" :aria-expanded "false" :data-bs-auto-close "outside"} "Filter"]
     [:form.dropdown-menu.p-4
      (field/search-input-field search :name "First name" "text")
      (field/search-input-field search :lastname "Last name" "text")
      (field/search-input-field search :patronymic "Patronymic" "text")
      (field/search-input-field search :insurance_id "Insurance id" "number")]]))

(defn control-elements []
  [:div.d-flex.align-items-center
   [:div.me-3 (create-view)]
   [:div.me-3 (search-view)]])

(defn app []
  [:div.container
   (control-elements)
   [:h1 "Patients database"]
   (patients-view)])

(defn ^:export init []
  (rf/dispatch-sync [::e/initialize])
  (r/render-component [app] (. js/document (getElementById "app"))))

(defn stop []
  (js/console.log "stop"))
