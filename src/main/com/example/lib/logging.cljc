(ns com.example.lib.logging
  "Some helpers to make logging a bit nicer."
  (:require
    ;; IMPORTANT: No explicit require for pprint in cljs. It bloats builds.
   #?@(:clj [[clojure.pprint :refer [pprint]]])
   [clojure.string :as str]
   [taoensso.encore :as enc]
   [taoensso.timbre :as log]))

#?(:clj
   (defmacro p
     "Convert a data structure to a visually-delimited pretty-printed string block."
     [v]
     `(str
       ~(str "\n" v "\n================================================================================\n")
       (with-out-str (pprint ~v))
       "================================================================================")))

(def ^:const redacted-placeholder "***REDACTED***")

(defn elide-sensitive
  "Substitui recursivamente valores de chaves em sensitive-keys por um placeholder.
   sensitive-keys é um set de keywords (ex: #{:password :com.example.model.account/password})."
  [sensitive-keys data]
  (cond
    (map? data)
    (reduce-kv
     (fn [m k v]
       (assoc m k (if (contains? sensitive-keys k) redacted-placeholder (elide-sensitive sensitive-keys v))))
     {}
     data)
    (vector? data) (mapv #(elide-sensitive sensitive-keys %) data)
    (seq? data)    (map #(elide-sensitive sensitive-keys %) data)
    :else          data))

(defn pretty
  "Marks a data item for pretty formatting when logging it (requires installing logging middleware)."
  [v]
  (with-meta v {:pretty true}))

(defn pretty-middleware [data->string]
  "Returns timbre logging middleware that will reformat items marked with `pretty` as pretty-printed strings using `data->string`."
  (fn [data]
    (update data :vargs (fn [args]
                          (mapv
                           (fn [v]
                             (if (and (coll? v) (-> v meta :pretty))
                               (data->string v)
                               v))
                           args)))))

#?(:clj
   (defn custom-output-fn
     "Derived from Timbre's default output function. Used server-side."
     ([data] (custom-output-fn nil data))
     ([opts data]
      (let [{:keys [no-stacktrace?]} opts
            {:keys [level ?err msg_ ?ns-str ?file timestamp_ ?line]} data]
        (format "%1.1S %s %40s:-%3s - %s%s"
                (name level)
                (force timestamp_)
                (str/replace-first (or ?ns-str ?file "?") "com.fulcrologic." "_")
                (or ?line "?")
                (force msg_)
                (enc/if-let [_   (not no-stacktrace?)
                             err ?err]
                  (str "\n" (log/stacktrace err opts))
                  ""))))))

#?(:clj
   (defn configure-logging!
     "Configure clojure logging for this project. `config` is the global config map that should contain
     `:taoensso.timbre/logging-config` as a key. Sensitive keys from pathom config are elided in all log output."
     [config]
     (let [{:keys [taoensso.timbre/logging-config]} config
           sensitive-keys (get-in config [:com.fulcrologic.rad.pathom/config :sensitive-keys] #{})
           elide-middleware
           (fn [data]
             (update data :vargs
                     (fn [args]
                       (mapv #(elide-sensitive sensitive-keys %) args))))]
       (log/merge-config! (assoc logging-config
                                 :middleware [elide-middleware
                                              (pretty-middleware #(with-out-str (pprint %)))]
                                 :output-fn custom-output-fn))
       (log/debug "Configured Timbre with " (p logging-config)))))

