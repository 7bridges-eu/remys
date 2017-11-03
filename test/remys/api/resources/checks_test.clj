(ns remys.api.resources.checks-test
  (:require [clojure.test :refer :all]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]))

(deftest test-table-exists?
  (testing "Testing table-exists? resource"
    (let [schema {:test "test"}]
      (is (= (true? (c/table-exists? schema :test))))
      (is (= (false? (c/table-exists? schema :testing)))))))

(deftest test-column-exists?
  (testing "Testing column-exists? resource"
    (let [schema {:test "test"}]
      (is (= (true? (c/column-exists? schema "test" "test"))))
      (is (= (false? (c/column-exists? schema "test" "testing")))))))

(deftest test-columns-exist?
  (testing "Testing columns-exist? resource"
    (let [schema {:test1 "test" :test2 "test"}]
      (is (= (true? (c/columns-exist? schema "test" ["test1" "test2"]))))
      (is (= (false? (c/columns-exist? schema "test" ["testing"])))))))

(deftest test-record-exists?
  (testing "Testing record-exists? resource"
    (let [schema {:test "test"}]
      (with-redefs [q/query-by-composite-key (fn [s t id] {:test "test"})
                    q/query-by-key (fn [s t id] {:test "test"})]
        (is (true? (c/record-exists? schema "test" "1")))
        (is (true? (c/record-exists? schema "test" "1___1")))))))

(deftest test-valid-query?
  (testing "Testing valid-query? resource"
    (let [valid-query "select * from test"
          invalid-query "drop table test"]
      (is (true? (c/valid-query? valid-query)))
      (is (false? (c/valid-query? invalid-query))))))
