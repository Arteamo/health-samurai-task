(ns health-samurai-task.rpc
  (:require
    [clojure.core :as core]
    [clojure.data.json :as json]
    [health-samurai-task.crud :as crud]))

(defn convert-json-request [content]
  (json/read-str content :key-fn keyword))

(defn convert-edn-request [content]
  (core/read-string content))

(defn- illegal-path [request-method uri]
  (throw (ex-info "404 NOT FOUND" {:method request-method :uri uri})))

(defn- get-content-keyword [query-params headers field]
  (if-let [type (not-empty (get query-params field (get headers field)))]
    (case type
      "application/json" :json
      "application/edn" :edn
      (throw (ex-info "Unknown mime-type" {:got type})))
    :json))

(defmulti wrap-request
          (fn [query-params headers _]
            (get-content-keyword query-params headers "content-type")))

(defmethod wrap-request :json [_ _ body]
  (-> body slurp convert-json-request (select-keys [:method :params])))

(defmethod wrap-request :edn [_ _ body]
  (-> body slurp convert-edn-request))

(defmulti wrap-response
          (fn [query-params headers _]
            (get-content-keyword query-params headers "accept")))

(def common-headers {"Access-Control-Allow-Origin"  "*"
                     "Access-Control-Allow-Methods" "POST, OPTIONS"
                     "Access-Control-Allow-Headers" "*"})

(defn- get-content-type [content-type]
  (case content-type
    :json "application/json"
    :edn "application/edn"))

(defn- build-headers [content-keyword]
  (assoc common-headers "Content-Type" (get-content-type content-keyword)))

(defmethod wrap-response :json [_ _ f]
  {:status  200
   :headers (build-headers :json)
   :body    (json/write-str {:result (f)})})

(defmethod wrap-response :edn [_ _ f]
  {:status  200
   :headers (build-headers :edn)
   :body    (str {:result (f)})})

(defn do-error [exception]
  (.printStackTrace exception)
  {:status  500
   :headers (build-headers :json)
   :body    (json/write-str {:error (.getMessage exception)})})

(defn wrap-rpc [query-params headers f]
  (try
    (wrap-response query-params headers f)
    (catch Throwable e (do-error e))))

(defn serve-rpc [state request]
  (let [{:keys [method params]} request]
    (case method
      "list" (crud/search state params)
      "create" (crud/create state params)
      "delete" (crud/delete state params)
      "update" (crud/update state params)
      "list-genders" crud/genders
      (throw (ex-info "Unknown method" {:method method})))))

(defn app [state request]
  (let [{:keys [request-method uri query-params headers body]} request
        wrapped-req (wrap-request query-params headers body)]
    (wrap-rpc query-params headers
              #(case [request-method uri]
                 [:post "/rpc"] (serve-rpc state wrapped-req)
                 [:options "/rpc"] {}
                 (illegal-path request-method uri)))))
