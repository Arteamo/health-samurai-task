(ns health-samurai-task.rpc
  (:require
    [clojure.core :as core]
    [clojure.data.json :as json]
    [health-samurai-task.crud :as crud])
  (:import (java.io EOFException)))

(defn- illegal-path [request-method uri]
  (throw (ex-info "404 NOT FOUND" {:method request-method :uri uri})))

(defn- get-content-keyword [query-params headers field]
  (if-let [type (not-empty (get query-params field (get headers field)))]
    (case type
      "application/json" :json
      "application/edn" :edn
      :json)
    :json))

(defmulti build-wrapped-body
          (fn [request]
            (let [{:keys [query-params headers]} request]
              (get-content-keyword query-params headers "content-type"))))

(defmethod build-wrapped-body :json [request]
  (-> (:body request) slurp (json/read-str :key-fn keyword) (select-keys [:method :params])))

(defmethod build-wrapped-body :edn [request]
  (-> (:body request) slurp core/read-string))

(defn- build-wrapped-req [request]
  (let [{:keys [query-params headers request-method uri]} request]
    {:body           (try (build-wrapped-body request) (catch EOFException _ {}))
     :request-method request-method
     :uri            uri
     :accept         (get-content-keyword query-params headers "accept")}))

(defn wrap-request [handler]
  (fn [request]
    (handler (build-wrapped-req request))))

(def common-headers {"Access-Control-Allow-Origin"  "*"
                     "Access-Control-Allow-Methods" "POST, OPTIONS"
                     "Access-Control-Allow-Headers" "*"})

(defn- get-content-type [content-type]
  (case content-type
    :json "application/json"
    :edn "application/edn"))

(defn- build-headers [content-keyword]
  (assoc common-headers "Content-Type" (get-content-type content-keyword)))

(defmulti do-result (fn [response] (:accept response)))

(defmethod do-result :json [result]
  {:status  200
   :headers (build-headers :json)
   :body    (json/write-str (select-keys result [:result]))})

(defmethod do-result :edn [result]
  {:status  200
   :headers (build-headers :edn)
   :body    (str (select-keys result [:result]))})

(defn do-error [exception]
  (.printStackTrace exception)
  {:status  500
   :headers (build-headers :json)
   :body    (json/write-str {:error (.getMessage exception)})})

(defn wrap-rpc [handler]
  (fn [request]
    (try
      (do-result (handler request))
      (catch Throwable e (do-error e)))))

(defn serve-rpc [state request]
  (let [{:keys [method params]} request]
    (case method
      "list" (crud/search state params)
      "create" (crud/create state params)
      "delete" (crud/delete state params)
      "update" (crud/update state params)
      "list-genders" crud/genders
      (throw (ex-info "Unknown method" {:method method})))))

(defn app [state wrapped-req]
  (let [{:keys [request-method uri body accept]} wrapped-req]
    {:result (case [request-method uri]
               [:post "/rpc"] (serve-rpc state body)
               [:options "/rpc"] {}
               (illegal-path request-method uri))
     :accept accept}))
