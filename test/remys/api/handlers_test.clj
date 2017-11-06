(ns remys.api.handlers-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]
            [remys.services.http :as http]
            [ring.mock.request :as mock]))

(deftest test-tables
  (testing "Testing /api/tables endpoint"
    (with-redefs [q/show-tables (fn [s] s)]
      (let [request (mock/request :get "/api/tables")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-all-fields
  (testing "Testing /api/:table endpoint with no query parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-all (fn [t] t)]
      (let [request (mock/request :get "/api/test")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-with-fields
  (testing "Testing /api/:table endpoint with query parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-fields (fn [t fs] t)]
      (let [request (mock/request :get "/api/test?fields=id")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-describe
  (testing "Testing /api/:table/describe endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/describe-table (fn [s t] t)]
      (let [request (mock/request :get "/api/test/describe")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-count-records
  (testing "Testing /api/:table/count endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/count-records (fn [t] t)]
      (let [request (mock/request :get "/api/test/count")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-id
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-by-key (fn [s t id] t)]
      (let [request (mock/request :get "/api/test/1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-composite-id
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-by-composite-key (fn [s t id] t)]
      (let [request (mock/request :get "/api/test/1___1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-dynamic
  (testing "Testing /api/dynamic endpoint"
    (with-redefs [c/valid-query? (fn [s] s)
                  q/execute-query (fn [s v] s)]
      (let [body (json/generate-string {:query "select * from test"})
            request (-> (mock/request :post "/api/dynamic" body)
                        (mock/content-type "application/json"))
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-update-table
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/record-exists? (fn [s t id] t)
                  c/columns-exist? (fn [s t cols] t)
                  q/update-table (fn [s t id params] params)]
      (let [body (json/generate-string {:test 1})
            request (-> (mock/request :put "/api/test/1" body)
                        (mock/content-type "application/json"))
            response (http/app request)]
        (is (= (:status response) 200))))))
