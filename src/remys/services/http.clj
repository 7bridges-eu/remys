(ns remys.services.http
  (:require [compojure.core :as compojure]
            [mount.core :as mount]
            [org.httpkit.server :as server]
            [ring.middleware.reload :as reload]))

(compojure/defroutes app
  (-> (compojure/routes "")
      reload/wrap-reload))

(defonce server (atom nil))

(defn stop-server! []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server! []
  (reset! server (server/run-server app {:port 3000})))

(mount/defstate http-server
  :start (start-server!)
  :stop (stop-server!))
