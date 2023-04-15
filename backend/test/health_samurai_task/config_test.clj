(ns health-samurai-task.config-test
  (:require [clojure.test :refer :all]
            [health-samurai-task.config :as config]))

(def db-host "EXPECTED_HOST")
(def db-port "8080")
(def db-name "crud_test")
(def expected-config {:host     db-host
                      :port     (Integer/parseInt db-port)
                      :dbname   db-name
                      :user     "crud_user"
                      :password "password"})

(defn- get-mocked-value [env-var]
  (case env-var
    "DB_HOST" db-host
    "DB_PORT" db-port
    "DB_NAME" db-name
    "DEFAULT"))

(deftest test-config-loading
  (testing "with mocked env vars"
    (with-redefs [config/read-var #(get-mocked-value %)]
      (is (= (config/load-config) expected-config)))))
