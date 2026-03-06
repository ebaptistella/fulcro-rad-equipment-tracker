(ns com.example.model.assignment
  (:require [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
            [com.fulcrologic.rad.type-support.date-time :as dt]
            [com.wsscode.pathom.connect :as pc]
            [com.fulcrologic.rad.attributes-options :as ao]
            #?(:clj [com.example.components.database-queries :as queries])
            #?(:clj [com.example.model.equipment :as equipment])
            #?(:clj [com.fulcrologic.rad.form :as form])
            #?(:clj [com.fulcrologic.rad.middleware.save-middleware :as save-middleware])
            #?(:cljs [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]])))

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

#?(:clj
   (pc/defresolver assignment-account-resolver [env {:assignment/keys [id]}]
     {::pc/input  #{:assignment/id}
      ::pc/output [{:assignment/account [:account/id :account/name]}]}
     (when-let [account (queries/get-assignment-account env id)]
       {:assignment/account account})))

#?(:clj
   (pc/defresolver assignment-equipment-resolver [env {:assignment/keys [id]}]
     {::pc/input  #{:assignment/id}
      ::pc/output [{:assignment/equipment [:equipment/id :equipment/serial :equipment/kind :equipment/kind-label]}]}
     (when-let [equipment (queries/get-assignment-equipment env id)]
       (let [kind-label (get equipment/equipment-kinds (:equipment/kind equipment) (name (:equipment/kind equipment)))]
         {:assignment/equipment (assoc equipment :equipment/kind-label kind-label)}))))

(defn- equipment-id-from-value [value]
  (cond
    (vector? value) (second value)
    (map? value) (or (:equipment/id value)
                     (when-let [after (get value :after)]
                       (if (vector? after) (second after) (:equipment/id after))))
    :else nil))

#?(:clj
   (defmethod save-middleware/rewrite-value :assignment/equipment
     [env entity-ident value]
     (when-let [equipment-id (equipment-id-from-value value)]
       (when (uuid? equipment-id)
         (let [[_ assignment-id] entity-ident
               existing (queries/get-current-assignment-for-equipment env equipment-id assignment-id)]
           (when existing
             (throw (ex-info "This equipment is already assigned to another account."
                             {:com.fulcrologic.rad.form/errors
                              [{:message "An equipment cannot be assigned to more than one account at the same time."}]}))))))
     value))

#?(:clj
   (pc/defmutation return-assignment [env {:assignment/keys [id]}]
     {::pc/params #{:assignment/id}
      ::pc/output [:assignment/id]}
     (form/save-form* env
                      {:com.fulcrologic.rad.form/id        id
                       :com.fulcrologic.rad.form/master-pk :assignment/id
                       :com.fulcrologic.rad.form/delta     {[:assignment/id id] {:assignment/returned-on {:after (dt/now)}}}})
     {:assignment/id id})
   :cljs
   (defmutation return-assignment [{:assignment/keys [id]}]
     (action [{:keys [state]}]
             (swap! state assoc-in [:assignment/id id :assignment/returned-on] (dt/now)))
     (remote [_] true)))

#?(:clj (def resolvers [return-assignment assignment-account-resolver assignment-equipment-resolver]))

(def attributes [id account equipment assigned-on returned-on all-assignments])
