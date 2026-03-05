(ns com.example.model.assignment
  (:require [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
            [com.fulcrologic.rad.attributes-options :as ao]
            [com.fulcrologic.rad.type-support.date-time :as dt]
            [com.wsscode.pathom.connect :as pc]
            #?(:clj [com.example.components.database-queries :as queries])))

(defattr id :assignment/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr account :assignment/account :ref
  {ao/target      :account/id
   ao/cardinality :one
   ao/required?   true
   ao/identities  #{:assignment/id}
   ao/schema      :production})

(defattr equipment :assignment/equipment :ref
  {ao/target      :equipment/id
   ao/cardinality :one
   ao/required?   true
   ao/identities  #{:assignment/id}
   ao/schema      :production})

(defattr assigned-on :assignment/assigned-on :instant
  {ao/required?  true
   ao/identities #{:assignment/id}
   ao/schema     :production
   ::dt/default-time-zone "America/Los_Angeles"})

(defattr returned-on :assignment/returned-on :instant
  {ao/identities #{:assignment/id}
   ao/schema     :production
   ::dt/default-time-zone "America/Los_Angeles"})

(defattr all-assignments :assignment/all-assignments :ref
  {ao/target    :assignment/id
   ::pc/output  [{:assignment/all-assignments [:assignment/id]}]
   ::pc/resolve (fn [env _]
                  #?(:clj
                     {:assignment/all-assignments (queries/get-all-assignments env nil)}))})

(def attributes [id account equipment assigned-on returned-on all-assignments])
