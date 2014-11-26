(ns portfolio.components
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn portfolio-component-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/a
        #js {:className   "list-group-item"}
        app))))

(conj [ 1 2] 1)

(defn portfolio-list-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)]
        (go-loop []
          (let [search-elem      (<! click-chan)
                add-component    (fn []
                                   (conj @app (:value search-elem)))]
            (om/transact! app add-component))
          (recur))))

    om/IRenderState
    (render-state [this state]
      (apply dom/div #js {:className "list-group portfolio-list"}
        (om/build-all
          portfolio-component-view
          (map
            (fn [elem] elem)
            app))))))

