(ns remys.api.handlers
  (:require [cheshire.core :as json]
            [clojure.walk :as w]
            [compojure.api.sweet :as api]
            [remys.api.resources.queries :as q]
            [remys.services.mysql :as db]
            [ring.util.http-response :as response]))

(api/defapi apis
  (api/context "/api" [table id fields]
    (api/GET "/tables" []
      (response/ok (q/show-tables @db/schema)))

    (api/GET "/:table" [table]
      :query-params [{fields :- String ""}]
      (if (q/table-exists? @db/schema table)
        (if (empty? fields)
          (response/ok (q/query-all table))
          (response/ok (q/query-fields table fields)))
        (response/not-found {:msg "Table not found"})))

    (api/GET "/:table/describe" [table]
      (if (q/table-exists? @db/schema table)
        (response/ok (q/describe-table @db/schema table))
        (response/not-found {:msg "Table not found"})))

    (api/GET "/:table/:id" [table id]
      (if (q/table-exists? @db/schema table)
        (if (re-matches #"[a-zA-Z0-9]+___[a-zA-Z0-9]+" id)
          (response/ok (q/query-by-composite-key @db/schema table id))
          (response/ok (q/query-by-key @db/schema table id)))
        (response/not-found {:msg "Table not found"})))

    (api/POST "/dynamic" []
      :body-params [query :- String
                    {params :- clojure.lang.PersistentArrayMap {}}]
      (if (q/valid-query? query)
        (try
          (response/ok (q/execute-query query params))
          (catch Exception e
            (response/not-found {:msg (str "Error: " e)})))
        (response/not-found {:msg "Query not valid"})))

    (api/PUT "/:table/:id" req
      (let [table (get-in req [:route-params :table])
            id (get-in req [:route-params :id])
            params (w/keywordize-keys (get req :body-params))
            cols (keys (get req :body-params))]
        (if (q/table-exists? @db/schema table)
          (if (q/record-exists? @db/schema table id)
           (if (q/columns-exist? @db/schema table cols)
             (response/ok (q/update-table @db/schema table id params))
             (response/not-found {:msg "Invalid columns"}))
           (response/not-found {:msg "Record not present"}))
          (response/not-found {:msg "Table not found"}))))))
