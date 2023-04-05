(ns health-samurai-task.core
  (:require
    [ring.adapter.jetty :refer [run-jetty]]
    [health-samurai-task.rpc :refer [app]]
    [health-samurai-task.config :refer [load-config]])
  (:gen-class))

(defn -main [& _]
  (load-config)
  (Class/forName "org.postgresql.Driver")
  (run-jetty app {:port 8080}))
