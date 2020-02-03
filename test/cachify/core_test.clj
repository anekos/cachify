(ns cachify.core-test
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.test :refer :all]
            [cachify.core :refer :all]
            [java-time :as tm]))


; https://stackoverflow.com/questions/21294294/run-tests-from-clojure-repl-and-leiningen
(defmacro with-osv ; with-out-str-and-value
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       (let [v# (do ~@body)]
         [(str s#) v#]))))

(defmacro with-temp-file [f & body]
  `(let [~f (java.io.File/createTempFile "cachify" ".dat")]
     ~@body
    (.delete ~f)))


(defn plus [a b]
  (print a "+" b)
  (+ a b))


(deftest cachify-test
  (testing "cache"
    (let [c-plus (cachify :plus plus)]
      (is (= ["1 + 2" 3]
             (with-osv (c-plus 1 2))))
      (is (= ["" 3]
             (with-osv (c-plus 1 2))))))

  (testing "ttl"
    (let [c-plus (cachify :plus plus :ttl (tm/seconds 1))]
      (is (= ["1 + 2" 3]
             (with-osv (c-plus 1 2))))
      (is (= ["" 3]
             (with-osv (c-plus 1 2))))
      (Thread/sleep 1500)
      (is (= ["1 + 2" 3]
             (with-osv (c-plus 1 2))))))

  (testing "result"
    (let [c-plus (cachify :plus +)]
      (is (= 3
             (c-plus 1 2)))
      (is (= 3
             (c-plus 1 2))))))

(deftest test-perm
  (testing "perm"
    (let [c-plus (cachify :plus plus)]
      (is (= ["4 + 4" 8]
             (with-osv (c-plus 4 4))))
      ; re-cachify without perm
      (is (= ["3 + 4" 7]
             (let [c-plus (cachify :plus plus)]
               (with-osv (c-plus 3 4))))))

    (binding [*cache-list* (atom (list))]
      (with-temp-file tempfn
        (.delete tempfn)
        (let [plus (cachify tempfn plus)]
          (is (= ["3 + 4" 7]
                 (with-osv (plus 3 4))))
          (perm)
          (is (= ["" 7]
                 (let [plus (cachify tempfn plus)]
                   (with-osv (plus 3 4))))))))))
