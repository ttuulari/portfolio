(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.util :as util]))

(defn draw [element input-data opts]
    (js/Chartist.Line.
      element
      (clj->js input-data)
      (clj->js (:graph-opts opts))
      (clj->js (:responsive-opts opts))))

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
      (dom/div (clj->js (:js opts))))))
