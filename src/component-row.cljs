(ns portfolio.component-row
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put!]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.graph :as graph]
    [portfolio.util :as util]
    [portfolio.slider :as slider])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


(defn remove-button-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/a #js {:className   "btn btn-danger btn-xs remove-button"
                  :onClick     (fn []
                                 (put!
                                   (:search-chan (om/get-shared owner))
                                   {:topic :remove-click
                                    :value app}))}
        "Remove"))))


(defn parse-input-num [elem]
  (apply str (filter (fn [c] (not (js/isNaN c))) (.trim elem))))

(defn handle-change
  "Grab the input element via the `input` reference."
  [owner state component-name]
  (let [value         (parse-input-num (.-value (om/get-node owner "term")))
        search-chan   (:search-chan  (om/get-shared owner))
        data          {:topic :component-amount
                       :value {:name component-name
                               :amount value}}]
    (put! search-chan data)
    (om/set-state! owner :text value)
    false))

(defn amount-input-view [app owner]
  (reify
    om/IInitState
    (init-state [_] {:text ""})

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "form-group col-sm-2"}
               (dom/div #js {:className "input-group"}
                        (dom/input #js {:className     "form-control"
                                        :type          "text"
                                        :autoComplete  "off"
                                        :ref           "term"
                                        :value         (:text state)
                                        :placeholder   (:amount app)
                                        :onChange      #(handle-change owner state (:name app))
                                        }))))))

(defn graph-input-data [prices]
   {:labels   (repeat (count prices) 0)
    :series   [prices]})

(defn portfolio-component-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div
        #js {:className   "list-group-item portfolio-component"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className   "portfolio-list-column col-sm-2"} (:name app))
          (om/build amount-input-view app)
          (dom/div #js {:className "portfolio-list-column col-sm-2"}
                   (util/to-fixed (last (:prices app)) 2))
          (dom/div #js {:className "portfolio-list-column col-sm-2"}
                   (util/to-fixed (* (:amount app) (last (:prices app))) 2))
          (om/build graph/graph-view
                    (graph-input-data (:prices app))
                    {:opts
                      {:js          {:className "ct-chart component-graph col-sm-2"}
                       :constructor (.-Line js/Chartist)
                       :graph-opts  {:width 200
                                    :height 80
                                    :showPoint false
                                    :lineSmooth false
                                    :axisX {:showLabel false
                                            :showGrid false}
                                    :axisY {:showLabel false
                                            :showGrid false}}}})
          (om/build remove-button-view app))))))
