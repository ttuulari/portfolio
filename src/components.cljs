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
      (dom/tr
        nil
        (dom/td nil app)
        (dom/td nil "616")
        (dom/td nil "700")))))

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
      (dom/table #js {:className "table table-striped table-hover portfolio-table"}
        (dom/thead nil
          (dom/tr nil
            (dom/th nil "Instrument")
            (dom/th nil "Amount")
            (dom/th nil "Price")))
        (apply dom/tbody nil
          (om/build-all
            portfolio-component-view
            (map
              (fn [elem] elem)
              app)))))))

