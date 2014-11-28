(ns portfolio.core
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put! timeout]]
    [portfolio.graph2 :as graph]
    [portfolio.input :as input]
    [portfolio.components :as components]
    [portfolio.search-results :as search-results]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def d1 [["mon" 1] ["tue" 2] ["wed" 3] ["thu" 4] ["fri" 5]])

(def app-state (atom {:results     []
                      :components  []
                      :data        d1}))

(def search-chan (chan))
(def notif-chan  (pub search-chan :topic))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/div #js {:className "search-container"}
          (om/build input/input-view true)
          (om/build search-results/results-view (:results app)))
        (om/build graph/graph-view (:data app))
        (om/build components/portfolio-list-view (:components app))))))

(om/root
  components-view
  app-state
  {:target (. js/document (getElementById "components"))
   :shared {:search-chan   search-chan
            :notif-chan    notif-chan}})

;(swap! app-state assoc :data [["mon" 3] ["tue" 8] ["wed" 5] ["thu" 13] ["fri" 12]])
