(defproject remys "0.1.0-SNAPSHOT"
  :description "remys: Rest APIs for MySQL databases"
  :url "http://lab.7bridges.eu/7b/remys"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[cheshire "5.8.0"]
                 [clj-time "0.14.0"]
                 [compojure "1.6.0"]
                 [hikari-cp "1.8.1"]
                 [http-kit "2.2.0"]
                 [metosin/compojure-api "1.1.11"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.11"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [org.clojure/tools.cli "0.3.5"]
                 [ring "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.1"]
                 [ring-middleware-format "0.7.2"]]
  :plugins [[lein-codox "0.10.3"]]
  :codox {:project {:name "remys" :package nil}}
  :main remys.core)
