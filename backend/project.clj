(defproject health-samurai-task "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [ring "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.5.4"]
                 [com.github.seancorfield/honeysql "2.4.1011"]
                 [org.clojure/math.combinatorics "0.2.0" :scope "test"]]
  :main health-samurai-task.core
  :aot [health-samurai-task.core])
