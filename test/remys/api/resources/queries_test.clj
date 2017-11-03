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
      (is (= (q/primary-key schema :test) (list "id"))))))

(deftest test-query-all
  (testing "Testing query-all resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-all :test) {:test "test"})))))

(deftest test-query-by-key
  (testing "Testing query-by-key resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
      (with-redefs [db/query! (fn [s] {:test "test"})]
       (is (= (q/query-by-key schema :test 1) {:test "test"}))))))

(deftest test-query-by-composite-key
  (testing "Testing query-by-composite-key resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id1"}
                         {:column-key "PRI" :column-name "id2"}]}]
      (with-redefs [db/query! (fn [s] {:test "test"})]
        (is (= (q/query-by-composite-key schema :test "1___1")
               {:test "test"}))))))

(deftest test-query-fields
  (testing "Testing query-fields resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-fields :test "id") {:test "test"})))))

(deftest test-execute-query
  (testing "Testing execute-query resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/execute-query "test" {:id 1}) {:test "test"})))))

(deftest test-update-table
  (testing "Testing update-table resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
        (with-redefs [db/update! (fn [s] {:test "test"})]
       (is (= (q/update-table schema "test" 1 {:id 1}) {:test "test"}))))))
