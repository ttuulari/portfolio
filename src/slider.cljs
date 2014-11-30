(ns portfolio.slider
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :as async
      :refer [put!]])
  (:use [jayq.core :only [$ css html]]))

(defn on-slide [elem owner data]
   (let [search-chan   (:search-chan  (om/get-shared owner))]
     (put! search-chan
          {:topic   :slide
           :data    data
           :value   (.val elem)})))

(defn draw [opts owner app]
  (let [element (om/get-node owner)
        $elem   ($ element)]
    (.noUiSlider $elem (clj->js (:slider opts)) true)
    (.on $elem "slide" (fn [] (on-slide $elem owner app)))))

(defn slider-view [app owner opts]
  (reify
    om/IDidMount
    (did-mount [this]
      (draw opts owner app))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (draw opts owner app))

    om/IRenderState
    (render-state [this state]
      (dom/div (clj->js (:js opts))))))
