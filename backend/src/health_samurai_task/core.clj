(ns health-samurai-task.core
  (:require
    [health-samurai-task.rpc :refer [app wrap-request wrap-rpc]]
    [health-samurai-task.config :refer [load-config]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn build-state []
  (let [db-config (load-config)]
    {:db db-config}))

(defn app-with-wrappers [state] (wrap-rpc (wrap-request (wrap-params #(app state %)))))

(defn -main [& _]
  (Class/forName "org.postgresql.Driver")
  (let [state (build-state)]
    (run-jetty (#'app-with-wrappers state) {:port 8080 :join? false})))
