(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(defn draw [element input-data]
  (let [[labels series]   (apply map vector input-data)
        data              {:labels labels
                           :series [series]}
        options           {:width 300
                           :height 300}
        responsive-opts   []]
    (js/Chartist.Line.
      element
      (clj->js data)
      (clj->js options)
      (clj->js responsive-opts))))

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
      (dom/div #js {:className "ct-chart portfolio-graph"}))))
