(ns portfolio.search-results
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn prefix? [to-test prefix]
  (= (.indexOf (.toLowerCase to-test) (.toLowerCase prefix)) 0))

(defn prefix->strs [prefix str-list]
  (let [pre?     (fn [elem] (prefix? elem prefix))
        filtered (filter pre? str-list)]
    (if (= (count filtered) (count str-list))
      []
      (vec filtered))))

(defn result-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/a
        #js {:className   "list-group-item"
             :onClick     (fn []
                              (put!
                                (:search-chan (om/get-shared owner))
                                {:topic :search-click
                                 :value app}))}
        app))))

(defn results-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [search-chan (sub (:notif-chan (om/get-shared owner)) :search (chan) false)
            click-chan  (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)]
        (go-loop []
          (let [[v c] (alts! [search-chan click-chan]
                             {:as {:default true}})
                reset-results    (fn [_] [])
                update-results   (fn [_]
                                   (prefix->strs (:value v) (keys (:data @app))))]
            (condp = c
              search-chan   (om/transact! app :results update-results)
              click-chan    (om/transact! app :results reset-results))
              :default      nil
            (recur)))))

    om/IRenderState
    (render-state [this state]
      (apply dom/div #js {:className "list-group search-results"}
        (om/build-all
          result-view
          (map
            (fn [elem] elem)
            (:results app)))))))
