(ns remys.api.handlers
  (:require [compojure.api.sweet :as api]
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
        (response/ok (q/query-by-id @db/schema table id))
        (response/not-found {:msg "Table not found"})))))
