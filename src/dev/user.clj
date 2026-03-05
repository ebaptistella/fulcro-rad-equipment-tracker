(ns user
  (:require
    [clj-reload.core :as reload]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]))

(reload/init {:no-reload '#{dataico.server-components.development-db
                            development}
              :dirs      ["src/dev" "src/main" "src/test"]})

(alter-var-root #'s/*explain-out* (constantly expound/printer))

