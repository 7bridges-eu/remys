(ns remys.api.handlers-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]
            [remys.services.http :as http]
            [ring.mock.request :as mock]))

(deftest tables-test
  (testing "Testing /api/tables endpoint"
    (with-redefs [q/show-tables (fn [s] s)]
      (let [request (mock/request :get "/api/tables")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-all-fields-test
  (testing "Testing /api/:table endpoint with no parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-all (fn [t] t)]
      (let [request (mock/request :get "/api/test")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-test
  (testing "Testing /api/:table endpoint with fields as parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  q/query-by-fields (fn [t fs] t)]
      (let [request (mock/request :get "/api/test?fields=id")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-and-like test
  (testing "Testing /api/:table endpoint with fields and like as parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  q/query-by-fields-and-like (fn [t fs l] t)]
      (let [request (mock/request :get "/api/test?fields=id&like=id")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-size-test
  (testing "Testing /api/:table endpoint with size as parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-by-size (fn [t s] t)]
      (let [request (mock/request :get "/api/test?size=10")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-offset-test
  (testing "Testing /api/:table endpoint with offset as parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-by-offset (fn [t o] t)]
      (let [request (mock/request :get "/api/test?offset=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-and-size-test
  (testing "Testing /api/:table endpoint with fields and size parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  c/string->number? (fn [o] o)
                  q/query-by-fields-and-size (fn [t fs s] s)]
      (let [request (mock/request :get "/api/test?fields=id&size=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-like-and-size-test
  (testing "Testing /api/:table endpoint with fields, like and size parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  c/string->number? (fn [o] o)
                  q/query-by-fields-like-and-size (fn [t fs l s] s)]
      (let [request (mock/request :get "/api/test?fields=id&like=i&size=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-and-offset-test
  (testing "Testing /api/:table endpoint with fields and offset parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  c/string->number? (fn [o] o)
                  q/query-by-fields-and-offset (fn [t fs o] o)]
      (let [request (mock/request :get "/api/test?fields=id&offset=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-like-and-offset-test
  (testing "Testing /api/:table endpoint with fields, like and offset parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  c/string->number? (fn [o] o)
                  q/query-by-fields-like-and-offset (fn [t fs l o] o)]
      (let [request (mock/request :get "/api/test?fields=id&like=i&offset=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-size-and-offset-test
  (testing "Testing /api/:table endpoint with fields, size and offset parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  c/string->number? (fn [s] s)
                  q/query-by-fields-size-and-offset (fn [t fs s o] o)]
      (let [request (mock/request :get "/api/test?fields=id&size=1&offset=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-with-fields-like-size-and-offset-test
  (testing "Testing /api/:table endpoint with fields, like, size and offset parameters"
    (with-redefs [c/table-exists? (fn [s t] t)
                  c/valid-query-fields? (fn [s t fs] t)
                  c/string->number? (fn [s] s)
                  q/query-by-fields-like-size-and-offset (fn [t fs l s o] o)]
      (let [request (mock/request :get "/api/test?fields=id&like=i&size=1&offset=1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-describe-test
  (testing "Testing /api/:table/describe endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/describe-table (fn [s t] t)]
      (let [request (mock/request :get "/api/test/describe")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest count-records-test
  (testing "Testing /api/:table/count endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/count-records (fn [t] t)]
      (let [request (mock/request :get "/api/test/count")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-id-test
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-by-key (fn [s t id] t)]
      (let [request (mock/request :get "/api/test/1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest table-composite-id-test
  (testing "Testing /api/:table/:id endpoint"
    (with-redefs [c/table-exists? (fn [s t] t)
                  q/query-by-composite-key (fn [s t id] t)]
      (let [request (mock/request :get "/api/test/1___1")
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest dynamic-query-test
  (testing "Testing /api/dynamic endpoint"
    (with-redefs [c/valid-query? (fn [s] s)
                  q/execute-query (fn [s v] s)]
      (let [body (json/generate-string {:query "select * from test"})
            request (-> (mock/request :post "/api/dynamic" body)
                        (mock/content-type "application/json"))
            response (http/app request)]
        (is (= (:status response) 200))))))

(deftest update-table-test
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
