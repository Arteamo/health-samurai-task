(ns health-samurai-task.rpc
  (:require
    [clojure.data.json :as json]
    [health-samurai-task.crud :as crud]))

(defn convert-request [content]
  (json/read-str content :key-fn keyword))

(defn serve-rpc [state request]
  (let [[method params] (-> request :body slurp convert-request (map [:method :params]))]
    (case method
      "list" (crud/search state params)
      "create" (crud/create state params)
      "delete" (crud/delete state params)
      "update" (crud/update state params)
      "list-genders" crud/genders
      (throw (ex-info "Unknown method" {:method method})))))

(defn illegal-path [_]
  {:error "404 NOT FOUND"})

(def common-headers {"Content-Type"                 "application/json"
                     "Access-Control-Allow-Origin"  "*"
                     "Access-Control-Allow-Methods" "POST, OPTIONS"
                     "Access-Control-Allow-Headers" "*"})

(defn do-result [result]
  {:status  200
   :headers common-headers
   :body    (json/write-str {:result result})})

(defn do-error [exception]
  (.printStackTrace exception)
  {:status  500
   :headers common-headers
   :body    (json/write-str {:error (.getMessage exception)})})

(defn wrap-json-rpc [f]
  (try
    (do-result (f))
    (catch Throwable e (do-error e))))

(defn app [state request]
  (wrap-json-rpc #(let [{:keys [request-method uri]} request]
                    (case [(name request-method) uri]
                      ["post" "/rpc"] (serve-rpc state request)
                      ["options" "/rpc"] {}
                      (illegal-path request)))))
