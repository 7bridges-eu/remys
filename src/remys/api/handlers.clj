(ns remys.api.handlers
  (:require [compojure.api.sweet :as api]
            [remys.services.mysql :as db]
            [ring.util.http-response :as response]))

(api/defapi apis
  {:coercion nil}

  (api/context
   "/api" [table id]
   (api/GET "/:table" [table]
            (response/ok (db/query "select * from test")))))
