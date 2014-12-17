(ns portfolio.container
  (:require
   [om-bootstrap.grid :as g]
   [om-tools.dom :as d :include-macros true]
   [om.core :as om :include-macros true]
   [portfolio.graph :as graph]
   [portfolio.input :as input]
   [portfolio.components :as components]
   [portfolio.search-results :as search-results]
   [portfolio.range-buttons :as range-buttons]
   [portfolio.portfolio-summary :as summary]
   [portfolio.util :as util]
   [portfolio.navbar :as navbar]
   [portfolio.slider :as slider]))

(defn render-container [app]
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
                         (g/row {:class "show-grid"}
                                (g/col {:xs 13 :md 8}
                                       (om/build graph/graph-view
                                                 (graph/graph-input-data app (graph/app-state->date-labels app))
                                                 {:opts
                                                  {:js          {:className "ct-chart portfolio-graph"
                                                                 :xs 8 :md 4}
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
                                                                                                  (util/to-fixed value 2))}}}})
                                       (om/build slider/slider-view
                                                 {:length (get-in app [:selected-date :total-length])
                                                  :range  (get-in app [:selected-date :range])}
                                                 {:opts
                                                  {:js
                                                   {:className "slider show slider-material-indigo"}}}))

                                (g/col {:xs 5 :md 3}
                                       (graph/build-legend (:components app))))))
           (g/row {:class "show-grid"}
                  (g/col {:xs 6 :md 4}
                         (d/div {:class "search-container"}
                                (om/build input/input-view true)
                                (om/build search-results/results-view app)))

                  (g/col {:xs 10 :md 6}
                         (om/build range-buttons/range-buttons-view
                                   (:selected-date app))))

           (d/div {:class "container portfolio-container"}
                  (g/row {:class "portfolio-container-row"}
                         (om/build components/portfolio-list-view app)))))))
