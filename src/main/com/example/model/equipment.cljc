(ns com.example.model.equipment
  (:require [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
            [com.fulcrologic.rad.attributes-options :as ao]
            [com.wsscode.pathom.connect :as pc]
            #?(:clj [com.example.components.database-queries :as queries])))

(def equipment-kinds {:equipment.kind/laptop    "Laptop"
                      :equipment.kind/monitor   "Monitor"
                      :equipment.kind/keyboard  "Keyboard"
                      :equipment.kind/mouse     "Mouse"
                      :equipment.kind/furniture "Furniture"})

(defattr id :equipment/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr kind :equipment/kind :enum
  {ao/identities         #{:equipment/id}
   ao/required?          true
   ao/enumerated-values  (set (keys equipment-kinds))
   ao/enumerated-labels  equipment-kinds
   ao/schema             :production})

(defattr serial :equipment/serial :string
  {ao/identities #{:equipment/id}
   ao/required?  true
   ao/schema     :production})

(defattr description :equipment/description :string
  {ao/identities #{:equipment/id}
   ao/schema     :production})

(defattr current-assignee :equipment/current-assignee :string
  {ao/identities #{:equipment/id}
   ao/read-only? true})

(defattr all-equipment :equipment/all-equipment :ref
  {ao/target    :equipment/id
   ::pc/output  [{:equipment/all-equipment [:equipment/id]}]
   ::pc/resolve (fn [env _]
                  #?(:clj
                     {:equipment/all-equipment (queries/get-all-equipment env nil)}))})

(defattr all-unassigned-equipment :equipment/all-unassigned-equipment :ref
  {ao/target    :equipment/id
   ::pc/output  [{:equipment/all-unassigned-equipment [:equipment/id]}]
   ::pc/resolve (fn [env _]
                  #?(:clj
                     {:equipment/all-unassigned-equipment (queries/get-unassigned-equipment env nil)}))})

#?(:clj
   (pc/defresolver equipment-current-assignment-resolver [env {:equipment/keys [id]}]
     {::pc/input  #{:equipment/id}
      ::pc/output [:equipment/current-assignee {:equipment/current-assignment [:assignment/id {:assignment/account [:account/id :account/name]}]}]}
     (when-let [assignment (queries/get-current-assignment-with-account-for-equipment env id)]
       {:equipment/current-assignee   (get-in assignment [:assignment/account :account/name])
        :equipment/current-assignment assignment})))

(def attributes [id kind serial description current-assignee all-equipment all-unassigned-equipment])

#?(:clj (def resolvers [equipment-current-assignment-resolver]))
