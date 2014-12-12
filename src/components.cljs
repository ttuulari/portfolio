(ns portfolio.components
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [portfolio.util :as util]
    [portfolio.component-row :as component-row]
    [portfolio.search-results :as search]
    [portfolio.slider :as slider]
    [om-bootstrap.panel :as p]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def columns [["Component" "col-sm-2"]
              ["Amount" "col-sm-1"]
              ["$ Price" "col-sm-1"]
              ["$ Position" "col-sm-1"]
              ["Chart" "col-sm-2"]])

(defn portfolio-list-column [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (d/h5 {:class   (str "portfolio-list-column " (last app))}
            (d/div nil (first app))))))

(defn portfolio-list-columns [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (d/div {:class "portfolio-list-columns"}
        (d/div {:class "row"}
          (om/build-all
            portfolio-list-column
            (map (fn [elem] elem) columns)))))))

(defn add-component [app value]
  (if (contains? (:components app) (:value value))
    app
    (assoc-in app [:components (:value value)] 0)))

(defn update-component [app value]
  (let [c-name   (get-in value [:value :name])
        c-amount (get-in value [:value :amount])]
    (if (contains? (:components app) c-name)
      (assoc-in app [:components c-name] c-amount)
      app)))

(defn remove-component [app value]
  (assoc app
    :components
    (dissoc (:components app) (get-in value [:value :name]))))

(defn portfolio-list-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan       (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)
            remove-chan      (sub (:notif-chan (om/get-shared owner)) :remove-click (chan) false)
            amount-chan      (sub (:notif-chan (om/get-shared owner)) :component-amount (chan) false)]

        (go-loop []
          (let [[value c]            (alts! [remove-chan click-chan amount-chan]
                                   {:as {:default true}})
                add-component    (fn [] (add-component @app value))
                update-component (fn [] (update-component @app value))
                remove-component (fn [] (remove-component @app value))]

            (condp = c
              remove-chan   (om/transact! app remove-component)
              click-chan    (om/transact! app add-component)
              amount-chan   (om/transact! app update-component)
              :default      nil))
            (recur))))

    om/IRenderState
    (render-state
      [this state]
      (d/div {:class "portfolio-container"}
             (p/panel
               {:header (d/div nil (om/build portfolio-list-columns nil))
                :list-group (d/ul {:class "list-group"}
                            (om/build-all
                              component-row/portfolio-component-view
                              (map
                                (fn [elem] {:name     (first elem)
                                            :amount   (second elem)
                                            :selected false
                                            :prices   (get-in app [:data (first elem) :prices])})
                                (:components app))))}
              nil)))))

