(ns com.example.ui.equipment-forms
  (:require [com.example.model.assignment :as assignment]
            [com.example.model.equipment :as equipment]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.rad.form :as form]
            [com.fulcrologic.rad.form-options :as fo]
            [com.fulcrologic.rad.picker-options :as po]
            [com.fulcrologic.rad.report :as report]
            [com.fulcrologic.rad.report-options :as ro]
            [com.fulcrologic.rad.type-support.date-time :as dt]))

(form/defsc-form EquipmentForm [this props]
  {fo/id            equipment/id
   fo/attributes    [equipment/kind
                     equipment/serial
                     equipment/description]
   fo/route-prefix  "equipment"
   fo/title         "Edit Equipment"})

(report/defsc-report EquipmentReport [this props]
  {ro/title               "Equipment Report"
   ro/source-attribute    :equipment/all-equipment
   ro/row-pk              equipment/id
   ro/columns             [equipment/kind equipment/serial equipment/description]
   ro/column-headings     {:equipment/kind "Kind"
                           :equipment/serial "Serial"
                           :equipment/description "Description"}
   ro/form-links          {equipment/serial EquipmentForm}
   ro/run-on-mount?       true
   ro/route               "equipment-report"})

(defsc AccountQuery [_ _]
  {:query [:account/id :account/name :account/email]
   :ident :account/id})

(defsc EquipmentQuery [_ _]
  {:query [:equipment/id :equipment/serial :equipment/kind]
   :ident :equipment/id})

(form/defsc-form AssignmentForm [this props]
  {fo/id             assignment/id
   fo/attributes     [assignment/account
                      assignment/equipment
                      assignment/assigned-on
                      assignment/returned-on]
   fo/field-styles   {:assignment/account   :pick-one
                      :assignment/equipment :pick-one
                      :assignment/assigned-on :datetime
                      :assignment/returned-on :datetime}
   fo/field-options  {:assignment/account   {po/query-key     :account/all-accounts
                                             po/query         [:account/id :account/name :account/email]
                                             po/query-component AccountQuery
                                             po/options-xform (fn [_ options]
                                                                (mapv (fn [{:account/keys [id name email]}]
                                                                        {:text (str name " (" email ")") :value [:account/id id]})
                                                                      (sort-by :account/name options)))
                                             po/cache-time-ms 30000}
                      :assignment/equipment {po/query-key     :equipment/all-unassigned-equipment
                                             po/query         [:equipment/id :equipment/serial :equipment/kind]
                                             po/query-component EquipmentQuery
                                             po/options-xform (fn [_ options]
                                                                (mapv (fn [{:equipment/keys [id serial kind]}]
                                                                        {:text (str (name kind) " - " serial) :value [:equipment/id id]})
                                                                      (sort-by :equipment/serial options)))
                                             po/cache-time-ms 30000}}
   fo/default-values {:assignment/assigned-on (dt/now)}
   fo/route-prefix   "assignment"
   fo/title          "Edit Assignment"})

(report/defsc-report AssignmentReport [this props]
  {ro/title               "Assignment Report"
   ro/source-attribute    :assignment/all-assignments
   ro/row-pk              assignment/id
   ro/row-query-inclusion [{:assignment/account [:account/name]}
                           {:assignment/equipment [:equipment/serial :equipment/kind-label]}]
   ro/columns             [assignment/account assignment/equipment assignment/assigned-on assignment/returned-on]
   ro/column-headings     {:assignment/account   "Account"
                           :assignment/equipment "Equipment"
                           :assignment/assigned-on "Assigned on"
                           :assignment/returned-on "Returned on"}
   ro/column-formatters   {:assignment/account   (fn [_ v] (if v (:account/name v) "-"))
                           :assignment/equipment (fn [_ v] (if v (str (:equipment/kind-label v) " " (:equipment/serial v)) "-"))
                           :assignment/assigned-on (fn [_ v] (when v (dt/inst->html-datetime-string v)))
                           :assignment/returned-on (fn [_ v] (if v (dt/inst->html-datetime-string v) "-"))}
   ro/form-links          {assignment/account AssignmentForm}
   ro/row-actions         [{:label     "Devolver"
                            :action    (fn [report-instance {:assignment/keys [id]}]
                                          #?(:cljs
                                             (comp/transact! report-instance [(assignment/return-assignment {:assignment/id id})])))
                            :disabled? (fn [_ row] (some? (:assignment/returned-on row)))}]
   ro/run-on-mount?       true
   ro/route               "assignment-report"})
