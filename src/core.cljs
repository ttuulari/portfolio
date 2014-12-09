  (ns portfolio.core
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put! timeout]]
    [portfolio.graph :as graph]
    [portfolio.input :as input]
    [portfolio.components :as components]
    [portfolio.search-results :as search-results]
    [portfolio.range-buttons :as range-buttons]
    [portfolio.portfolio-summary :as summary]
    [portfolio.navbar :as navbar]
    [portfolio.slider :as slider]
    [portfolio.util :as util]
    [portfolio.prices :as prices]
    [om.core :as om :include-macros true]
    [om-bootstrap.grid :as g]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def range-data (range-buttons/update-range "2014-05-30" -30))

(def app-state (atom {:selected-date    range-data
                      :results          []
                      :components       {}
                      :data             prices/prices}))

(def search-chan (chan))
(def notif-chan  (pub search-chan :topic))

(def date-labels (graph/app-state->date-labels @app-state))

(def last-date-index
  (dec (count date-labels)))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (d/div
      {:class "grids-examples"}
      (om/build navbar/navbar-view true)
      (g/grid
       {}
       (g/row {:class "show-grid"}
              (g/col {:xs 6 :md 4}
                     (d/div {:class "summary-container"}
                            (om/build summary/portfolio-summary-view app)))
              (g/col {:xs 12 :md 8}
                     (d/div {:class "graph-slider"}
                            (om/build graph/graph-view
                                      (graph/graph-input-data app date-labels)
                                      {:opts
                                       {:js          {:className "ct-chart portfolio-graph"}
                                        :constructor (.-Line js/Chartist)
                                        :graph-opts  {:width 600
                                                      :height 300
                                                      :lineSmooth false
                                                      :showPoint false
                                                      :chartPadding 20
                                                      :axisX {
                                                              :showLabel true}
                                                      :axisY {
                                                              :scaleMinSpace 50
                                                              :labelInterpolationFnc (fn [value]
                                                                                       (util/to-fixed value 1))}}}})
                            (om/build slider/slider-view
                                      {:length (get-in app [:selected-date :total-length])
                                       :range  (get-in app [:selected-date :range])}
                                      {:opts
                                       {:js
                                        {:className "slider-material-red shorf range-slider"}}})
                            (om/build range-buttons/range-buttons-view
                                      (:selected-date app)))))
       (g/row {:class "show-grid"}
              (g/col {:xs 6 :md 4}
                     (d/div {:class "search-container"}
                            (om/build input/input-view true)
                            (om/build search-results/results-view app))))
       (d/div {:class "container portfolio-container"}
              (g/row {:class "portfolio-container-row"}
                     (om/build components/portfolio-list-view app))))))))

(om/root
  components-view
  app-state
  {:target (. js/document (getElementById "components"))
   :shared {:search-chan   search-chan
            :notif-chan    notif-chan}})

;(swap! app-state assoc :data [["mon" 3] ["tue" 8] ["wed" 5] ["thu" 13] ["fri" 12]])
