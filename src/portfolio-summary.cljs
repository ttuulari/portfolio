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

(defn build-summary
  [prices index]
    {:value         (last prices)
     :yield         (price/yield
                      (first prices)
                      (last prices))
     :std           (price/sample-std (price/return-ratio prices))
     :sharpe        (price/sharpe (price/return-ratio prices) (price/return-ratio index))
     :pain-to-gain  (price/gain-to-pain prices)})

(defn portolio-summary-data
  [input]
  (let [mult-price    (fn [[name amount]]
                        (map (fn [elem] (* amount elem))
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
  (let [base   [{:topic "Start date: " :value (util/date-delta-days->str (:end-date selected-date)
                                                                         (:range selected-date))}
                {:topic "End date: "   :value (:end-date selected-date)}]]
       (cond-> base
               (and (contains? data :value) (not (zero? (:value data))))
               (conj base
                     {:topic "Value $ "
                      :value (util/to-fixed (:value data) 2)})

               (and (contains? data :yield) (not (zero? (:yield data))))
               (conj base
                     {:topic "Yield % "
                      :value (util/to-fixed (:yield data) 2)})

               (and (contains? data :std) (not (js/isNaN (:std data))))
               (conj base
                     {:topic "Return STD "
                      :value (util/to-fixed (:std data) 4)})

               (and (contains? data :sharpe) (not (js/isNaN (:sharpe data))))
               (conj base
                     {:topic "Sharpe "
                      :value (util/to-fixed (:sharpe data) 4)})

               (and (contains? data :pain-to-gain) (not (js/isNaN (:pain-to-gain data))))
               (conj base
                     {:topic "Gain-to-pain "
                      :value (util/to-fixed (:pain-to-gain data) 4)})
               )))


(defn summary-row-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
                  (d/li {:class "list-group-item"}
                        (d/div {:class "row"}
                               (d/div {:class "col-sm-5"} (:topic app))
                               (d/div {:class "col-sm-6"} (:value app)))))))

(defn portfolio-summary-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
    (let [data           (portolio-summary-data app)
          selected-date  (:selected-date app)
          rows           (summary-items selected-date data)]
      (d/div {:class "portfolio-summary"}
             (p/panel
              {:header "Your Portfolio"
               :list-group (d/ul {:class "list-group"}
                                 (om/build-all summary-row-view rows))}
              nil))))))
