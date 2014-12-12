(ns portfolio.component-row
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put!]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.graph :as graph]
    [portfolio.util :as util]
    [portfolio.slider :as slider]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn remove-button-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
                  (d/a {:class   "btn btn-danger btn-xs remove-button  col-sm-1"
                        :on-click     (fn []
                                        (put!
                                         (:search-chan (om/get-shared owner))
                                         {:topic :remove-click
                                          :value app}))}
                       "Remove"))))

(defn parse-input-num [elem]
  (-> elem
      .trim
      util/filter-nans
      (util/strip " ")
      (util/l-trim "0")))

(defn handle-change
  "Grab the input element via the `input` reference."
  [owner state component-name]
  (let [value         (parse-input-num (.-value (om/get-node owner "term")))
        search-chan   (:search-chan  (om/get-shared owner))
        data          {:topic :component-amount
                       :value {:name component-name
                               :amount value}}]
    (set! (.-value (om/get-node owner "term")) value)
    (put! search-chan data)
    false))

(defn amount-input-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "form-group col-sm-1"}
               (dom/div #js {:className "input-group"}
                        (dom/input #js {:className     "form-control"
                                        :type          "text"
                                        :autoComplete  "off"
                                        :ref           "term"
                                        :value         (:amount app)
                                        :placeholder   (:amount app)
                                        :onChange      #(handle-change owner state (:name app))
                                        }))))))

(defn graph-input-data [prices]
   {:labels   (repeat (count prices) 0)
    :series   [prices]})

(defn button-data [owner app]
  (let [base-class   ["btn" "btn-xs" "remove-button" "col-sm-1"]
        button-data  {:on-click  (fn []
                                   (put!
                                    (:search-chan (om/get-shared owner))
                                    {:topic :compare-click
                                     :value app}))}]
    (if (:selected app)
      (assoc button-data :class
        (clojure.string/join
         " "
         (concat base-class ["btn-info"])))
      (assoc button-data :class
        (clojure.string/join
         " "
         (concat base-class ["btn-primary" "btn-raised"]))))))

(defn button-text [app]
  (if (:selected app)
    "Deselect"
    "Compare"))

(defn togglebutton-view [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (d/a (button-data owner app) (button-text app)))))

(defn portfolio-component-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
                  (d/li {:class "list-group-item portfolio-component"}
                        (d/div {:class "portfolio-list-column col-sm-2"} (:name app))
                        (om/build amount-input-view app)
                        (d/div {:class "portfolio-list-column col-sm-1"}
                               (util/to-fixed (last (:prices app)) 2))
                        (d/div {:class "portfolio-list-column col-sm-1"}
                               (util/to-fixed (* (:amount app) (last (:prices app))) 2))
                        (om/build graph/graph-view
                                  (graph-input-data (:prices app))
                                  {:opts
                                   {:js          {:className "ct-chart component-graph col-sm-2"}
                                    :constructor (.-Line js/Chartist)
                                    :graph-opts  {:width 180
                                                  :height 80
                                                  :showPoint false
                                                  :lineSmooth false
                                                  :axisX {:showLabel false
                                                          :showGrid false}
                                                  :axisY {:showLabel false
                                                          :showGrid false}}}})
                        (om/build togglebutton-view app)
                        (om/build remove-button-view app)))))

