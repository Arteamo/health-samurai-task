(ns health-samurai-task.config
  (:require [clojure.spec.alpha :as s]
            [clojure.edn :as edn]
            [health-samurai-task.common :as common])
  (:import (clojure.lang RT)))

(defn- parse-var-name [str]
  (let [groups (re-matches #"\{\{(.*)\}\}" str)]
    (if (= (count groups) 2)
      (second groups)
      (throw (ex-info "Illegal params" {:field str})))))

(defn- read-var [str]
  (System/getenv str))

(defn- get-string-var [str]
  (-> str parse-var-name read-var))

(defn- get-long-var [str]
  (-> str parse-var-name read-var Long/parseLong))

(s/def ::->env-string
  (s/and
    ::common/ne-string
    (common/with-conformer get-string-var)))

(s/def ::->env-long
  (s/and
    ::common/ne-string
    (common/with-conformer get-long-var)))

(def config-atom (atom nil))
(def config-file "config.edn")

(s/def :db/host ::->env-string)
(s/def :db/port ::->env-long)
(s/def :db/user ::common/ne-string)
(s/def :db/password ::common/ne-string)

(s/def ::conf
  (s/keys :req-un [:db/host
                   :db/port
                   :db/user
                   :db/password]))

(defn- read-from-jar [path]
  (with-open [r (RT/resourceAsStream (RT/baseLoader) path)]
    (slurp r)))

(defn- coerce-config [conf]
  (common/validate-or-throw ::conf conf))

(defn- set-config [conf]
  (reset! config-atom (merge conf)))

(defn load-config []
  (-> config-file read-from-jar edn/read-string coerce-config set-config))
