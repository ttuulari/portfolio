(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(def options {:xaxis
                {:minorTickFreq 2}
              :grid {:minorVerticalLines false}})

(defn draw [element data]
  (js/Flotr.draw
    element
    (clj->js data) (clj->js options)))

(defn graph-view [app owner]
  (reify
    om/IDidMount
    (did-mount [this]
      (draw (om/get-node owner) app))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (draw (om/get-node owner) app))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "graph-view"}))))
