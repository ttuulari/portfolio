(ns portfolio.core
  (:require
    [portfolio.graph :as graph]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(def d1 [[0 3] [4 8] [8 5] [9 13] [11 10]])
(def d2 [[0 5] [4 1] [8 10] [9 13] [11 6]])

(def app-state (atom {:components  []
                      :data [d1 d2]}))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/button #js {} "Jepaje")
        (apply dom/div #js {:className "base"}
          (om/build-all graph/graph-view [(:data app)]))))))

(om/root components-view app-state
  {:target (. js/document (getElementById "components"))})


(def d3 [[0 7] [4 50] [8 10] [9 13] [11 6]])
(swap! app-state assoc :data [d2 d1])
