(ns remys.api.routes
  (:require [remys.api.handlers :as h]
            [compojure.core :as c]
            [ring.middleware.format :as f]))

(c/defroutes routes
  (-> h/apis
      (c/wrap-routes f/wrap-restful-format)))
