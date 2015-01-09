(ns portfolio.test.component
  (:require-macros [cemerick.cljs.test :refer (is deftest done with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [portfolio.test.common :as com]
            [cljs.core.async :as async
             :refer [<! >! chan put! pub sub]]
            [portfolio.navbar :as navbar]
            [portfolio.search-results :as results])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(deftest navbar-renders?
  (let [data true]
    (testing "navbar class found"
      (is (= 1
             (let [c (com/new-container!)]
               (om/root navbar/navbar-view true {:target c})
               (count  (sel c ".navbar"))))))))

(deftest ^:async search-results-renders?
  (testing "Empty container renders"
    (is (= 1
           (let [c           (com/new-container!)
                 search-chan (chan)
                 notif-chan  (pub search-chan :topic)
                 prec        {:target c
                              :shared {:notif-chan    notif-chan
                                       :search-chan   search-chan}}]
             (om/root results/results-view [] prec)
             (done)
             (count (sel c :div.search-results)))
           1))))
