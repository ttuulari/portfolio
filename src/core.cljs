(ns portfolio.core
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put! timeout]]
    [portfolio.graph :as graph]
    [portfolio.input :as input]
    [portfolio.components :as components]
    [portfolio.search-results :as search-results]
    [portfolio.slider-indicator :as indicator]
    [portfolio.slider :as slider]
    [portfolio.util :as util]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
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

(def app-state (atom {:selected-point   {:date "2014-05-30"}
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

(defn graph-input-data
  [input]
  (let [labels        date-labels
        mult-price    (fn [[name amount]]
                        (map (fn [elem] (* amount elem))
                             (:prices (get (:data input) name))))

        prices        (map mult-price (:components input))]

    (if (empty? prices)
      {:labels   labels
       :series   [(repeat (count labels) 0.0)]}
      {:labels   labels
       :series   [(apply map + prices)]})))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/div nil
          (dom/div #js {:className "search-container"}
            (om/build input/input-view true)
            (om/build search-results/results-view app))
          (dom/div #js {:js {:className "graph-slider"}}
            (om/build graph/graph-view
                      (graph-input-data app)
                      {:opts
                        {:js          {:className "ct-chart portfolio-graph"}
                         :constructor (.-Line js/Chartist)
                         :graph-opts  {:width 600
                                      :height 300
                                      :showPoint false
                                      :chartPadding 20
                                      :axisX {}
                                      :axisY {
                                        :labelInterpolationFnc (fn [value]
                                                                 (util/to-fixed value 1))}}}})
            (dom/div nil
              (om/build indicator/slider-indicator-view app)
              (om/build slider/slider-view
                {:labels   date-labels}
                {:opts
                  {:js       {:className "slider-material-red shor col-sm-2"}
                   :slider   {:start last-date-index
                              :step 1
                              :connect "lower"
                              :range {:min 0
                                      :max last-date-index}}}}))))
          (om/build components/portfolio-list-view app)))))

(om/root
  components-view
  app-state
  {:target (. js/document (getElementById "components"))
   :shared {:search-chan   search-chan
            :notif-chan    notif-chan}})

;(swap! app-state assoc :data [["mon" 3] ["tue" 8] ["wed" 5] ["thu" 13] ["fri" 12]])
