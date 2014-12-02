(ns portfolio.component-row
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan pub put!]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
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

(defn submit [owner component-name]
  (let [value         (parse-input-num (.-value (om/get-node owner "term")))
        search-chan   (:search-chan  (om/get-shared owner))
        data          {:topic :amount
                       :value {:name component-name
                               :amount value}}]
    (.log js/console (clj->js value))
    (put! search-chan data)
    false))

(defn amount-input-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "form-group col-sm-2"}
               (dom/div #js {:className "input-group"}
                        (dom/input #js {:className     "form-control"
                                        :type          "text"
                                        :autoComplete  "off"
                                        :ref           "term"
                                        :placeholder   (:amount app)
                                        :onChange (fn [] (submit owner (:name app)))
                                        }))))))

(defn portfolio-component-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div
        #js {:className   "list-group-item portfolio-component"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className   "portfolio-list-column col-sm-2"} (:name app))
          (om/build amount-input-view app)
          (dom/div #js {:className   "portfolio-list-column col-sm-2"} (:price app))
          (om/build remove-button-view app))))))
