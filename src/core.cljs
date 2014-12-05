(ns portfolio.core
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put! timeout]]
    [portfolio.graph :as graph]
    [portfolio.input :as input]
    [portfolio.components :as components]
    [portfolio.search-results :as search-results]
    [portfolio.slider-indicator :as indicator]
    [portfolio.range-buttons :as range-buttons]
    [portfolio.portfolio-summary :as summary]
    [portfolio.slider :as slider]
    [portfolio.util :as util]
    [om.core :as om :include-macros true]
    [om-bootstrap.grid :as g]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def prices {"Ford Motor Co."
               {:dates   ["2014-05-21" "2014-05-22" "2014-05-23" "2014-05-27" "2014-05-28" "2014-05-29" "2014-05-30"]
                :prices  [15.91 15.91 16.02 16.16 16.31 16.54 16.44]}
             "Morjens Co."
               {:dates   ["2014-05-21" "2014-05-22" "2014-05-23" "2014-05-27" "2014-05-28" "2014-05-29" "2014-05-30"]
                :prices  [15.91 15.91 16.02 16.16 16.31 16.54 16.44]}
             "Mokia Corporation"
               {:dates   ["2014-05-21" "2014-05-22" "2014-05-23" "2014-05-27" "2014-05-28" "2014-05-29" "2014-05-30"]
                :prices  [7.62 7.80 7.84 7.86 7.85 7.92 8.13]}
             })

(def range-data (range-buttons/update-range "2014-05-30" -7))

(def app-state (atom {:selected-date    range-data
                      :results          []
                      :components       {}
                      :data             prices}))

(def search-chan (chan))
(def notif-chan  (pub search-chan :topic))

(def date-labels
  (-> @app-state
      :data
      first
      second
      :dates))

(def last-date-index
  (dec (count date-labels)))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (d/div {:class "grids-examples"}
        (g/grid {}
          (g/row {:class "show-grid"}
            (g/col {:xs 6 :md 4}
              (d/div {:class "search-container"}
                (om/build input/input-view true)
                (om/build search-results/results-view app)
                (om/build summary/portfolio-summary-view app)))
            (g/col {:xs 12 :md 8}
              (d/div {:class "graph-slider"}
                (om/build range-buttons/range-buttons-view
                          (:selected-date app))
                (om/build graph/graph-view
                          (graph/graph-input-data app date-labels)
                          {:opts
                            {:js          {:className "ct-chart portfolio-graph"}
                             :constructor (.-Line js/Chartist)
                             :graph-opts  {:width 600
                                          :height 300
                                          :showPoint false
                                          :chartPadding 20
                                          :axisY {
                                            :labelInterpolationFnc (fn [value]
                                                                     (util/to-fixed value 1))}}}})
                (g/row {}
                             (g/col {:xs 6 :md 4 :class "offset2"}
                                    (om/build slider/slider-view
                                              {:length (get-in app [:selected-date :length])
                                               :range  (get-in app [:selected-date :range])}
                                              {:opts
                                               {:js       {:className "slider-material-red shorf"}
                                                :slider   {:start last-date-index
                                                           :step 1
                                                           :connect "lower"
                                                           :range {:min 0
                                                                   :max last-date-index}}}}))
                             (g/col {:xs 6 :md 4}
                      (om/build indicator/slider-indicator-view (:selected-date app)))))))
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
