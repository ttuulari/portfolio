(ns portfolio.range-buttons
  (:require
   [om-bootstrap.button :as b]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :as async :refer [put!]]))

(defn range-buttons-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "container range-buttons"}
      (b/toolbar {:className "col-md-1"}
        (b/button {:bs-style "danger"} "1 Week")
        (b/button {:bs-style "danger"} "1 Month")
        (b/button {:bs-style "danger"} "6 Months")
        (b/button {:bs-style "danger"} "1 Year"))))))
