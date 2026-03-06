(ns com.example.components.database-queries
  (:require
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    [datomic.client.api :as d]
    [taoensso.timbre :as log]
    [taoensso.encore :as enc]))

(defn- env->db [env]
  (some-> env (get-in [do/databases :production]) (deref)))

(defn get-all-accounts
  [env query-params]
  (if-let [db (env->db env)]
    (let [ids (if (:show-inactive? query-params)
                (d/q '[:find ?uuid
                       :where
                       [?dbid :account/id ?uuid]] db)
                (d/q '[:find ?uuid
                       :where
                       [?dbid :account/active? true]
                       [?dbid :account/id ?uuid]] db))]
      (mapv (fn [[id]] {:account/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-all-tags
  [env _]
  (let [db (doto (env->db env) assert)
        ids (d/q '[:find ?uuid :where [?dbid :tag/id ?uuid]] db)]
    (mapv (fn [[id]] {:tag/id id}) ids)))

(defn get-all-items
  [env {:category/keys [id]}]
  (if-let [db (env->db env)]
    (let [ids (if id
                (d/q '[:find ?uuid
                       :in $ ?catid
                       :where
                       [?c :category/id ?catid]
                       [?i :item/category ?c]
                       [?i :item/id ?uuid]] db id)
                (d/q '[:find ?uuid
                       :where
                       [_ :item/id ?uuid]] db))]
      (mapv (fn [[id]] {:item/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-customer-invoices [env {:account/keys [id]}]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?uuid
                     :in $ ?cid
                     :where
                     [?dbid :invoice/id ?uuid]
                     [?dbid :invoice/customer ?c]
                     [?c :account/id ?cid]] db id)]
      (mapv (fn [[id]] {:invoice/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-all-invoices
  [env query-params]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?uuid
                     :where
                     [?dbid :invoice/id ?uuid]] db)]
      (mapv (fn [[id]] {:invoice/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-invoice-customer-id
  [env invoice-id]
  (if-let [db (env->db env)]
    (ffirst
      (d/q '[:find ?account-uuid
             :in $ ?invoice-uuid
             :where
             [?i :invoice/id ?invoice-uuid]
             [?i :invoice/customer ?c]
             [?c :account/id ?account-uuid]] db invoice-id))
    (log/error "No database atom for production schema!")))

(defn get-all-categories
  [env query-params]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?id
                     :where
                     [?e :category/label]
                     [?e :category/id ?id]] db)]
      (mapv (fn [[id]] {:category/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-all-equipment
  [env _query-params]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?uuid
                     :where
                     [?e :equipment/id ?uuid]] db)]
      (mapv (fn [[id]] {:equipment/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-all-assignments
  [env _query-params]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?uuid
                     :where
                     [?e :assignment/id ?uuid]] db)]
      (mapv (fn [[id]] {:assignment/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-assignment-account
  "Returns the account map (id and name) for an assignment, for report display."
  [env assignment-id]
  (if-let [db (env->db env)]
    (some-> (d/pull db [{:assignment/account [:account/id :account/name]}] [:assignment/id assignment-id])
            :assignment/account)
    (log/error "No database atom for production schema!")))

(defn get-assignment-equipment
  "Returns the equipment map (id, serial, kind) for an assignment. Normalizes :equipment/kind
   from Datomic ref {:db/ident k} to keyword. kind-label is added by the assignment resolver."
  [env assignment-id]
  (if-let [db (env->db env)]
    (let [raw (some-> (d/pull db [{:assignment/equipment [:equipment/id :equipment/serial {:equipment/kind [:db/ident]}]}] [:assignment/id assignment-id])
                      :assignment/equipment)]
      (when raw
        (update raw :equipment/kind
                (fn [k] (cond (keyword? k) k
                             (map? k)     (:db/ident k)
                             :else        k)))))
    (log/error "No database atom for production schema!")))

(defn get-unassigned-equipment
  "Returns equipment ids that have no current assignment (no assignment with nil returned-on)."
  [env _query-params]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?equipment-id
                     :where [?eq :equipment/id ?equipment-id]
                     (not-join [?eq]
                               [?a :assignment/equipment ?eq]
                               [(missing? $ ?a :assignment/returned-on)])]
                   db)]
      (mapv (fn [[id]] {:equipment/id id}) ids))
    (log/error "No database atom for production schema!")))

(defn get-current-assignment-for-equipment
  "Returns the assignment id that currently has the given equipment (no returned-on), or nil.
   exclude-assignment-id: when updating an assignment, pass its id so it is not considered a conflict."
  [env equipment-id exclude-assignment-id]
  (if-let [db (env->db env)]
    (let [result (if exclude-assignment-id
                   (d/q '[:find ?assignment-id .
                          :in $ ?equipment-id ?exclude-id
                          :where [?eq :equipment/id ?equipment-id]
                                 [?a :assignment/equipment ?eq]
                                 [(missing? $ ?a :assignment/returned-on)]
                                 [?a :assignment/id ?assignment-id]
                                 [(not= ?assignment-id ?exclude-id)]]
                        db equipment-id exclude-assignment-id)
                   (d/q '[:find ?assignment-id .
                          :in $ ?equipment-id
                          :where [?eq :equipment/id ?equipment-id]
                                 [?a :assignment/equipment ?eq]
                                 [(missing? $ ?a :assignment/returned-on)]
                                 [?a :assignment/id ?assignment-id]]
                        db equipment-id))]
      result)
    (do (log/error "No database atom for production schema!")
        nil)))

(defn get-current-assignment-with-account-for-equipment
  "Returns {:assignment/id _ :assignment/account {:account/id _ :account/name _}} for the current assignment of this equipment, or nil."
  [env equipment-id]
  (if-let [db (env->db env)]
    (when-let [assignment-id (get-current-assignment-for-equipment env equipment-id nil)]
      (d/pull db [:assignment/id {:assignment/account [:account/id :account/name]}] [:assignment/id assignment-id]))
    (log/error "No database atom for production schema!")))

(defn get-open-assignment-ids-for-account
  "Returns a seq of assignment ids that belong to this account and have no returned-on (currently assigned)."
  [env account-id]
  (if-let [db (env->db env)]
    (let [ids (d/q '[:find ?assignment-id
                     :in $ ?account-id
                     :where [?a :assignment/account ?acc]
                            [?acc :account/id ?account-id]
                            [?a :assignment/id ?assignment-id]
                            [(missing? $ ?a :assignment/returned-on)]]
                   db account-id)]
      (mapv first ids))
    (do (log/error "No database atom for production schema!")
        [])))

(defn get-equipment-count-for-account
  "Returns the number of equipment items currently assigned to this account (assignments with no returned-on)."
  [env account-id]
  (if-let [db (env->db env)]
    (let [n (d/q '[:find (count ?a) .
                   :in $ ?account-id
                   :where [?a :assignment/account ?acc]
                          [?acc :account/id ?account-id]
                          [(missing? $ ?a :assignment/returned-on)]]
                 db account-id)]
      (or n 0))
    (do (log/error "No database atom for production schema!")
        0)))

(defn get-line-item-category [env line-item-id]
  (if-let [db (env->db env)]
    (let [id (ffirst
               (d/q '[:find ?cid
                      :in $ ?line-item-id
                      :where
                      [?e :line-item/id ?line-item-id]
                      [?e :line-item/item ?item]
                      [?item :item/category ?c]
                      [?c :category/id ?cid]] db line-item-id))]
      id)
    (log/error "No database atom for production schema!")))

(defn get-login-info
  "Get the account name, time zone, and password info via a username (email)."
  [env username]
  (enc/if-let [db (log/spy :info (env->db env))]
    (d/pull db [:account/name
                {:time-zone/zone-id [:db/ident]}
                :password/hashed-value
                :password/salt
                :password/iterations]
      [:account/email username])))
