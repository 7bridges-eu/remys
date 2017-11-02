(ns remys.api.handlers-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [remys.api.resources.queries :as q]
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
    (with-redefs [q/table-exists? (fn [s t] t)
                  q/query-all (fn [t] t)]
      (let [request (mock/request :get "/api/test")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-with-fields
  (testing "Testing /api/:table endpoint with query parameters"
    (with-redefs [q/table-exists? (fn [s t] t)
                  q/query-fields (fn [t fs] t)]
      (let [request (mock/request :get "/api/test?fields=id")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-describe
  (testing "Testing /api/:table/describe endpoint"
    (with-redefs [q/table-exists? (fn [s t] t)
                  q/describe-table (fn [s t] t)]
      (let [request (mock/request :get "/api/test/describe")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-id
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [q/table-exists? (fn [s t] t)
                  q/query-by-key (fn [s t id] t)]
      (let [request (mock/request :get "/api/test/1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest test-table-composite-id
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [q/table-exists? (fn [s t] t)
                  q/query-by-composite-key (fn [s t id] t)]
      (let [request (mock/request :get "/api/test/1___1")
            response (http/app request)]
        (is (= (:status response) 200))))))
