(ns portfolio.test.price-utils
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]
            [portfolio.price-utils :as p]))

(deftest yield-no-change
  (is (=
       (p/yield 1 1)
       0)))
