(ns health-samurai-task.core
  (:require
    [ring.adapter.jetty :refer [run-jetty]]
    [health-samurai-task.rpc :refer [app]]
    [health-samurai-task.config :refer [load-config]])
  (:gen-class))

(defn build-state []
  (let [db-config (load-config)]
    {:db db-config}))

(defn -main [& _]
  (Class/forName "org.postgresql.Driver")
  (let [state (build-state)]
    (run-jetty #(#'app state %) {:port 8080 :join? false})))
