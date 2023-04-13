(ns frontend.core
  (:require [reagent.core :as r]
            [frontend.client :as client]
            [frontend.util :as config]
            [frontend.fields :as field]
            [frontend.util :as util]))

(def default-creation-data {:name         ""
                            :lastname     ""
                            :patronymic   ""
                            :gender       config/default-gender
                            :address      ""
                            :birthday     ""
                            :insurance_id ""})

(defn submit-creation [e patient-atom]
  (.preventDefault e)
  (client/create-patient @patient-atom)
  (reset! patient-atom default-creation-data))

(defn create-view []
  (let [patient (r/atom default-creation-data)]
    [(fn []
       [:div.dropdown
        [:button.btn.btn-primary.dropdown-toggle
         {:data-bs-toggle "dropdown" :aria-expanded "false" :data-bs-auto-close "outside"} "Create new"]
        [:form.dropdown-menu.p-4 {:on-submit (fn [e] (submit-creation e patient))}
         (field/creation-input-field patient :name "First name" "text" true)
         (field/creation-input-field patient :lastname "Last name" "text" true)
         (field/creation-input-field patient :patronymic "Patronymic" "text" false)
         (field/gender-field patient)
         (field/creation-input-field patient :address "Address" "text" true)
         (field/creation-input-field patient :birthday "Birthday" "date" true)
         (field/creation-input-field patient :insurance_id "Insurance id" "number" true)
         [:input.btn.btn-primary {:type "submit" :value "Create"}]]])]))

(defn submit-edit [e patient-atom]
  (.preventDefault e)
  (client/update-patient @patient-atom))

(defn edit-view [patient]
  (let [insurance_id (:insurance_id patient)
        gender (:gender patient)
        edit-atom (r/atom {:insurance_id insurance_id :gender gender})]
    [(fn []
       [:div.dropdown {:key (:insurance_id patient)}
        [:button.btn.btn-primary.dropdown-toggle
         {:data-bs-toggle "dropdown" :aria-expanded "false" :data-bs-auto-close "outside"} "Edit"]
        [:form.dropdown-menu.p-4 {:on-submit (fn [e] (submit-edit e edit-atom))}
         (field/edit-input-field edit-atom :name "First name" "text")
         (field/edit-input-field edit-atom :lastname "Last name" "text")
         (field/edit-input-field edit-atom :patronymic "Patronymic" "text")
         (field/gender-field edit-atom)
         (field/edit-input-field edit-atom :address "Address" "text")
         [:input.btn.btn-primary {:type "submit" :value "Save"}]]])]))

(defn patient-view [patient]
  (let [insurance-id (:insurance_id patient)]
    [:tr {:key (:insurance_id patient)}
     [:td (:name patient)]
     [:td (:lastname patient)]
     [:td (:patronymic patient)]
     [:td (-> patient :gender util/gender-view)]
     [:td (:birthday patient)]
     [:td (:address patient)]
     [:td insurance-id]
     [:td [:button.btn.btn-danger {:onClick (fn [] (client/delete-patient insurance-id))} "Delete"]]
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
      (let [result (:result @client/patients-atom)]
        (for [patient result]
          (patient-view patient))))]])

(defn search-view []
  [:div.dropdown
   [:button.btn.btn-primary.dropdown-toggle
    {:data-bs-toggle "dropdown" :aria-expanded "false" :data-bs-auto-close "outside"} "Filter"]
   [:form.dropdown-menu.p-4
    (field/search-input-field client/search-atom :name "First name" "text")
    (field/search-input-field client/search-atom :lastname "Last name" "text")
    (field/search-input-field client/search-atom :patronymic "Patronymic" "text")
    (field/search-input-field client/search-atom :insurance_id "Insurance id" "number")]])

(defn control-elements []
  [:div.d-flex.align-items-center
   [:div.me-3 (create-view)]
   [:div.me-3 (search-view)]])

(defn app []
  [:div.container
   (control-elements)
   [:h1 "Patients database"]
   (patients-view)])

(defn start []
  (r/render-component [app] (. js/document (getElementById "app"))))

(defn ^:export init []
  (start)
  (client/get-patients)
  (client/get-genders))

(defn stop []
  (js/console.log "stop"))
