(ns portfolio.component-row
  "Om component and related functions to handle a single portfolio stock."
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put!]]
    [om.core :as om :include-macros true]
    [portfolio.graph :as graph]
    [portfolio.util :as util]
    [portfolio.slider :as slider]
    [om-bootstrap.button :as b]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn remove-button-view [app owner]
  "Om component for portfolio component remove button."
  (reify
    om/IRenderState
    (render-state [this state]
                  (d/a {:class   "btn btn-danger btn-xs remove-button col-sm-1"
                        :on-click     (fn []
                                        (put!
                                         (:search-chan (om/get-shared owner))
                                         {:topic :remove-click
                                          :value app}))}
                       "Remove"))))

(defn parse-input-num [elem]
  "Parse component amount input."
  (-> elem
      .trim
      util/filter-nans
      (util/strip " ")
      (util/l-trim "0")))

(defn handle-change
  "Input amount chage handler. Grab the input element via the `input` reference."
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
  "Om component for portfolio component amount input."
  (reify
    om/IRenderState
    (render-state
     [this state]
     (d/div  {:class "form-group col-sm-1"}
             (d/div {:class "input-group"}
                    (d/input {:class     "form-control"
                              :type          "text"
                              :auto-complete  "off"
                              :ref           "term"
                              :value         (:amount app)
                              :placeholder   (:amount app)
                              :on-change      #(handle-change owner state (:name app))
                              }))))))

(defn graph-input-data [prices]
  "Construct empty graph input data."
  {:labels   (repeat (count prices) 0)
   :series   [prices]})

(defn button-data [owner app]
  "Construct button based on selected status."
  (let [base-class   ["btn" "btn-xs"]
        button-data  {:on-click  (fn []
                                   (put!
                                    (:search-chan (om/get-shared owner))
                                    {:topic :compare-click
                                     :value app}))}]
    (if (:selected app)
      (assoc button-data :class
        (clojure.string/join
         " "
         (concat base-class ["btn-material-bluegrey"])))
      (assoc button-data :class
        (clojure.string/join
         " "
         (concat base-class ["btn-material-indigo" "btn-raised"]))))))

(defn button-text [app]
  "Button text based on selected status."
  (if (:selected app)
    "Deselect"
    "Compare"))

(defn togglebutton [app owner]
  "Component select for comparison button."
  (d/a (button-data owner app)
       (button-text app)))

(defn remove-button [app owner]
  "Component remove button."
  (d/a {:class   "btn btn-material-grey btn-xs"
        :on-click     (fn []
                        (put!
                         (:search-chan (om/get-shared owner))
                         {:topic :remove-click
                          :value app}))}
       "Remove"))

(defn portfolio-component-view [app owner]
  "Om component single portfolio component."
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
                        (d/div {:class "row-buttons col-sm-3"}
                               (b/toolbar {}
                                          (togglebutton app owner)
                                          (remove-button app owner)))))))

