(ns portfolio.core
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put! timeout]]
    [portfolio.graph :as graph]
    [portfolio.input :as input]
    [portfolio.components :as components]
    [portfolio.search-results :as search-results]
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

(def app-state (atom {:results     []
                      :components  {}
                      :data        prices}))

(def search-chan (chan))
(def notif-chan  (pub search-chan :topic))

(defn graph-input-data
  [input]
  (let [labels        (-> input
                          :data
                          first
                          second
                          :dates)

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
          (om/build graph/graph-view
                    (graph-input-data app)
                    {:opts
                      {:js          {:className "ct-chart portfolio-graph"}
                       :constructor (.-Line js/Chartist)
                       :graph-opts  {:width 500
                                    :height 300
                                    :showPoint false
                                    :axisY {
                                      :labelInterpolationFnc (fn [value]
                                                               (util/to-fixed value 2))}}}})
        (om/build components/portfolio-list-view app))))))

(om/root
  components-view
  app-state
  {:target (. js/document (getElementById "components"))
   :shared {:search-chan   search-chan
            :notif-chan    notif-chan}})

;(swap! app-state assoc :data [["mon" 3] ["tue" 8] ["wed" 5] ["thu" 13] ["fri" 12]])
