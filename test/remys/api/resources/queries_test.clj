(ns remys.api.resources.queries-test
  (:require [remys.api.resources.queries :as q]
            [remys.services.mysql :as db]
            [clojure.test :refer :all]))

(deftest test-show-tables
  (testing "Testing show-tables resource"
    (let [schema {:test "test"}]
      (is (= (q/show-tables schema) (list :test))))))

(deftest test-describe-table
  (testing "Testing describe-table resource"
    (let [schema {:test "test"}]
      (is (= (q/describe-table schema :test) "test")))))

(deftest test-table-exists?
  (testing "Testing table-exists? resource"
    (let [schema {:test "test"}]
      (is (= (true? (q/table-exists? schema :test))))
      (is (= (false? (q/table-exists? schema :testing)))))))

(deftest test-primary-key
  (testing "Testing primary-key resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
      (is (= (q/primary-key schema :test) "id")))))

(deftest test-query-all
  (testing "Testing query-all resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-all :test) {:test "test"})))))

(deftest test-query-by-id
  (testing "Testing query-by-id resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
      (with-redefs [db/query! (fn [s] {:test "test"})]
       (is (= (q/query-by-id schema :test 1) {:test "test"}))))))

(deftest test-query-fields
  (testing "Testing query-fields resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-fields :test "id") {:test "test"})))))
