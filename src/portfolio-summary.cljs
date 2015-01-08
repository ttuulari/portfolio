(ns portfolio.portfolio-summary
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om-bootstrap.panel :as p]
    [portfolio.util :as util]
    [portfolio.price-utils :as price]
    [portfolio.prices :as prices-db]
    [portfolio.graph :as graph]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def summary-decimals 3)

(defn build-summary
  "Build summary data based on portfolio and index price sequences."
  [prices index-price]
  (let [summary   [{:topic         "Value $"
                    :value         (last prices)}

                   {:topic         "Yield %"
                    :value         (price/yield
                                    (first prices)
                                    (last prices))}

                   {:topic         "Return STD"
                    :value         (price/sample-std (price/return-ratio prices))}

                   {:topic         "Sharpe"
                    :value         (price/sharpe (price/return-ratio prices) (price/return-ratio index-price))}

                   {:topic         "Gain-to-pain"
                    :value         (price/gain-to-pain prices)}]


        filter-zero (filter (fn [e] (not (zero? (:value e)))))
        filter-nan  (filter (fn [e] (not (js/isNaN (:value e)))))
        fix-prec    (map (fn [e]
                           (assoc
                             e
                             :value
                             (util/to-fixed (:value e)
                                            summary-decimals))))
        tx          (comp
                     filter-zero
                     filter-nan
                     fix-prec)]
    (into [] tx summary)))

(defn portfolio-summary-data
  "Construct summary data based on state."
  [input]
  (let [mult-price    (fn [[name data]]
                        (map (fn [elem] (* (:amount data) elem))
                             (:prices (get-in input [:data name]))))

        prices        (map mult-price (:components input))
        total-prices  (if (empty? prices)
                        []
                        (apply map + prices))
        window-prices (graph/window-input (graph/app-state->date-labels input)
                                          (:selected-date input)
                                          total-prices)
        window-index  (graph/window-input (graph/app-state->date-labels input)
                                          (:selected-date input)
                                          prices-db/index)]
    (when (not (empty? window-prices))
      (build-summary window-prices window-index))))

(defn summary-items [selected-date data]
  (let [base   [{:topic "Start date"
                 :value (util/date-delta-days->str (:end-date selected-date)
                                                   (:range selected-date))}
                {:topic "End date"
                 :value (:end-date selected-date)}]]
    (into base data)))

(defn summary-row-view
  "Portfolio summary item Om component"
  [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (d/li {:class "list-group-item"}
           (d/div {:class "row"}
                  (d/div {:class "col-sm-5"} (:topic app))
                  (d/div {:class "col-sm-6"} (:value app)))))))

(defn portfolio-summary-view
  "Portfolio summary Om component"
  [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (let [data           (portfolio-summary-data app)
           selected-date  (:selected-date app)
           rows           (summary-items selected-date data)]
       (d/div {:class "portfolio-summary"}
              (p/panel
               {:header "Your Portfolio"
                :list-group (d/ul {:class "list-group"}
                                  (om/build-all summary-row-view rows))}
               nil))))))
