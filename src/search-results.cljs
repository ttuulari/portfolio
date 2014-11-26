(ns portfolio.search-results
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! timeout]]
    [portfolio.graph :as graph]
    [portfolio.input :as input]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def names ["noksu" "wärre" "warre"])

(defn prefix? [to-test prefix]
  (= (.indexOf to-test (.toLowerCase prefix)) 0))

(defn prefix->strs [prefix str-list]
  (let [pre?   (fn [elem] (prefix? elem prefix))]
    (filter pre? str-list)))

(defn result-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div nil "Jepa"))))

(defn results-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [search-chan  (:search-chan (om/get-shared owner))]
        (go-loop []
          (let [search-elem          (<! search-chan)]
            (when (= (:op search-elem) :search)
              (.log js/console (clj->js (prefix->strs (:value search-elem) names))))
            (recur)))))

    om/IRenderState
    (render-state [this state]
      (apply dom/div #js {:className "list-group search-results"}
        (om/build-all
          result-view
          [1])))))
