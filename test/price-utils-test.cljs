(ns portfolio.test.price-utils
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [portfolio.price-utils :as p]))

(deftest yield-no-change
  (is (=
       (p/yield 1 1)
       0)))

(deftest yield-halves
  (is (=
       (p/yield 2 1)
       -50)))

(deftest yield-doubles
  (is (=
       (p/yield 1 2)
       100)))

(deftest sample-variance-zero
  (is (=
       (p/sample-variance [1 1])
       0)))

(deftest sample-variance-half
  (is (=
       (p/sample-variance [1 2])
       0.5)))

(deftest std-is-root-variance
  (is (=
       (Math.pow (p/sample-variance [1 2]) 0.5)
       (p/sample-std [1 2]))))

(deftest return-ratio-is-last-first-yield
  (let [yield-ratio    (->
                        (p/yield 2 1)
                        (+ 100)
                        (/ 100))]
  (is (=
       yield-ratio
       (first (p/return-ratio [2 1]))))))
