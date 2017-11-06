(ns remys.api.resources.checks-test
  (:require [clojure.test :refer :all]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]))

(deftest table-exists?-test
  (testing "Testing table-exists? resource"
    (let [schema {:test "test"}]
      (is (true? (c/table-exists? schema :test)))
      (is (false? (c/table-exists? schema :testing))))))

(deftest column-exists?-test
  (testing "Testing column-exists? resource"
    (let [schema {"test" [{:column-name "test"}]}]
      (is (true? (c/column-exists? schema "test" "test")))
      (is (false? (c/column-exists? schema "test" "testing"))))))

(deftest columns-exist?-test
  (testing "Testing columns-exist? resource"
    (let [schema {"test" [{:column-name "test1"} {:column-name "test2"}]}]
      (is (true? (c/columns-exist? schema "test" ["test1" "test2"])))
      (is (false? (c/columns-exist? schema "test" ["testing"]))))))

(deftest record-exists?-test
  (testing "Testing record-exists? resource"
    (let [schema {:test "test"}]
      (with-redefs [q/query-by-composite-key (fn [s t id] {:test "test"})
                    q/query-by-key (fn [s t id] {:test "test"})]
        (is (true? (c/record-exists? schema "test" "1")))
        (is (true? (c/record-exists? schema "test" "1___1")))))))

(deftest valid-query?-test
  (testing "Testing valid-query? resource"
    (let [valid-query "select * from test"
          invalid-query "drop table test"]
      (is (true? (c/valid-query? valid-query)))
      (is (false? (c/valid-query? invalid-query))))))

(deftest string->number?-test
  (testing "Testing string->number? resource"
    (is (= "42" (c/string->number? "42")))
    (is (nil? (c/string->number? "test")))))

(deftest valid-query-fields?-test
  (testing "Testing valid-query-fields? resource"
    (let [schema {"test" [{:column-name "test1"} {:column-name "test2"}]}]
      (is (true? (c/valid-query-fields? schema "test" "test1,test2")))
      (is (false? (c/valid-query-fields? schema "test" "testing1,testing2"))))))
