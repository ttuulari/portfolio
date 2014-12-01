(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(defn draw [element input-data opts]
  (let [{:keys [dates prices]} input-data
        labels            dates
        data              {:labels labels
                           :series [prices]}
        responsive-opts   []]
    (js/Chartist.Line.
      element
      (clj->js data)
      (clj->js (:graph opts))
      (clj->js responsive-opts))))

(defn graph-view [app owner opts]
  (reify
    om/IDidMount
    (did-mount [this]
      (draw (om/get-node owner) app opts))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (draw (om/get-node owner) app opts))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "ct-chart portfolio-graph"}))))
