(ns portfolio.search-results
  "Om component displaying list group of search results."
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
   [portfolio.util :as util]
    [om.core :as om :include-macros true]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn prefix?
  "Predicate for testing lowercase prefix."
  [to-test prefix]
  (= (.indexOf (.toLowerCase to-test)
               (.toLowerCase prefix))
     0))

(defn prefix->strs
  "Filter results by prefix."
  [prefix str-list]
  (let [pre?     (fn [elem] (prefix? elem prefix))
        filtered (filter pre? str-list)]
    (if (= (count filtered) (count str-list))
      []
      (vec filtered))))

(defn result-view
  "Search result om component."
  [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (d/a
      {:class        "list-group-item search-result"
       :on-click     (fn []
                       (put!
                        (:search-chan (om/get-shared owner))
                        {:topic :search-click
                         :value app}))}
      app))))

(defn results-view
  "List of search results om component."
  [app owner]
  (reify
    om/IWillMount
    (will-mount
     [this]
     (let [search-chan (sub (:notif-chan (om/get-shared owner)) :search (chan) false)
           click-chan  (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)]
       (go-loop
        []
        (let [[v c] (alts! [search-chan click-chan]
                           {:as {:default true}})
              reset-results    (fn [_] [])
              update-results   (fn [_]
                                 (prefix->strs (:value v)
                                               (keys (:data @app))))]
          (condp = c
            search-chan   (om/transact! app :results update-results)
            click-chan    (om/transact! app :results reset-results))
          :default      nil
          (recur)))))

    om/IRenderState
    (render-state
     [this state]
     (d/div {:class "list-group search-results"}
            (om/build-all
             result-view
             (map
              (fn [elem] elem)
              (:results app)))))))
