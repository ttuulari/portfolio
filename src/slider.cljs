(ns portfolio.slider
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:use [jayq.core :only [$ css html]]))

(defn on-slide [elem]
  (.log js/console (.val elem)))

(defn draw [element opts]
  (let [$elem   ($ element)]
    (.noUiSlider $elem (clj->js (:slider opts)))
    (.on $elem "slide" (fn [] (on-slide $elem)))))

(defn slider-view [app owner opts]
  (reify
    om/IDidMount
    (did-mount [this]
      (draw (om/get-node owner) opts))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (draw (om/get-node owner) opts))

    om/IRenderState
    (render-state [this state]
      (dom/div (clj->js (:js opts))))))
