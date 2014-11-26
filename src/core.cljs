(ns portfolio.core
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! timeout]]
    [portfolio.graph :as graph]
    [portfolio.input :as input]
    [portfolio.search-results :as search-results]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def d1 [[0 3] [4 8] [8 5] [9 13] [11 10]])
(def d2 [[0 5] [4 1] [8 10] [9 13] [11 6]])

(def app-state (atom {:components  []
                      :data [d1 d2]}))

(def search-chan (chan))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (om/build input/input-view true)
        (om/build search-results/results-view true)
        (dom/div #js {:className "base-view"}
          (om/build graph/graph-view (:data app)))))))

(om/root
  components-view
  app-state
  {:target (. js/document (getElementById "components"))
   :shared {:search-chan   search-chan}})


(def d3 [[0 7] [4 50] [8 10] [9 13] [11 6]])
(swap! app-state assoc :data [d1 d2])
