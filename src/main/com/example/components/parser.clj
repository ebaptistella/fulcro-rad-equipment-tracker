(ns com.example.components.parser
  (:require
   [com.example.components.auto-resolvers :refer [automatic-resolvers]]
   [com.example.model.timezone :as timezone]
   [com.example.components.blob-store :as bs]
   [com.example.components.config :refer [config]]
   [com.example.components.datomic :refer [datomic-connections]]
   [com.example.components.delete-middleware :as delete]
   [com.example.components.save-middleware :as save]
   [com.example.model :refer [all-attributes]]
   [com.example.model.account :as account]
   [com.example.model.assignment :as assignment]
   [com.example.model.equipment :as equipment]
   [com.example.model.invoice :as invoice]
   [com.fulcrologic.rad.attributes :as attr]
   [com.fulcrologic.rad.blob :as blob]
   [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
   [com.fulcrologic.rad.form :as form]
   [com.fulcrologic.rad.pathom :as pathom]
   [mount.core :refer [defstate]]
   [com.example.model.sales :as sales]
   [com.example.model.item :as item]
   [com.wsscode.pathom.core :as p]
   [com.fulcrologic.rad.type-support.date-time :as dt]
   [com.wsscode.pathom.connect :as pc]))

(pc/defresolver index-explorer [{::pc/keys [indexes]} _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (p/transduce-maps
    (remove (comp #{::pc/resolve ::pc/mutate} key))
    indexes)})

(def ^:private default-timezone "America/Los_Angeles")

(defn- timezone-for-env
  "Resolves the timezone string for the current request from the session (account-based).
   Session stores :time-zone/zone-id as set on login from the account's timezone.
   Falls back to default-timezone when not logged in or no timezone in session."
  [env]
  (let [zone (get-in env [:ring/request :session :time-zone/zone-id])]
    (cond
      (string? zone) zone
      (keyword? zone) (get timezone/datomic-time-zones zone default-timezone)
      :else default-timezone)))

(defstate parser
  :start
  (pathom/new-parser config
                     [(attr/pathom-plugin all-attributes)
                      (form/pathom-plugin save/middleware delete/middleware)
                      (datomic/pathom-plugin (fn [env] {:production (:main datomic-connections)}))
                      (blob/pathom-plugin bs/temporary-blob-store {:files         bs/file-blob-store
                                                                   :avatar-images bs/image-blob-store})
                      {::p/wrap-parser
                       (fn transform-parser-out-plugin-external [parser]
                         (fn transform-parser-out-plugin-internal [env tx]
                           (dt/with-timezone (timezone-for-env env)
                             (if (and (map? env) (seq tx))
                               (parser env tx)
                               {}))))}]
                     [automatic-resolvers
                      form/resolvers
                      (blob/resolvers all-attributes)
                      account/resolvers
                      assignment/resolvers
                      equipment/resolvers
                      invoice/resolvers
                      item/resolvers
                      sales/resolvers
                      timezone/resolvers
                      index-explorer]))
