(ns portfolio.slider
  "Om component for a slider. Uses noUISlider JS library."
  (:require
    [portfolio.util :as util]
    [om.core :as om :include-macros true]
    [om-tools.dom :as d :include-macros true]
    [cljs.core.async :as async
      :refer [put!]])
  (:use [jayq.core :only [$]]))

(defn on-slide
  "On-slide handler. Add slide event handling here, e.g. core.async channel put!."
  [elem owner]
   (let [search-chan   (:search-chan  (om/get-shared owner))]
     (put! search-chan
          {:topic   :slide
           :value   (.val elem)})))

(defn app->slider
  "Map cursor app state to slider data."
  [app]
  (let [slider-range   (dec (:length app))]
    {:start slider-range
     :connect "lower"
     :step 1
     :range {:min 0
             :max slider-range}})  )

(defn draw
  "Do the JS draw."
  [owner app]
  (let [element (om/get-node owner)
        $elem   ($ element)]
    (.noUiSlider $elem (clj->js (app->slider app)) true)
    (.on $elem "slide" (fn [] (on-slide $elem owner)))
    (.val $elem (dec (:length app)))))

(defn slider-view
  "Slider om component."
  [app owner opts]
  (reify
    om/IDidMount
    (did-mount [this]
      (draw owner app))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (draw owner app))

    om/IRenderState
    (render-state [this state]
      (d/div (clj->js (:js opts))))))
