(ns remys.services.http
  (:require [compojure.core :as compojure]
            [mount.core :as mount]
            [remys.api.routes :as api]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :as reload]))

(compojure/defroutes app
  (-> (compojure/routes api/routes)
      reload/wrap-reload))

(defonce server (jetty/run-jetty #'app {:port 3000 :join? false}))

(defn stop-server! []
  (.stop server))

(defn start-server! []
  (.start server))

(mount/defstate http-server
  :start (start-server!)
  :stop (stop-server!))
