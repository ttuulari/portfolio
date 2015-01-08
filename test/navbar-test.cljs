(ns portfolio.test.navbar
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [portfolio.test.common :as com]
            [portfolio.navbar :as navbar])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(deftest navbar-renders?
  (let [data true]
    (testing "navbar class found"
      (is (= 1
             (let [c (com/new-container!)]
               (om/root navbar/navbar-view true {:target c})
               (count  (sel c ".navbar"))))))))
