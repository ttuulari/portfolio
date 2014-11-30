(ns portfolio.components
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.component-row :as component-row]
    [portfolio.slider :as slider])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def columns ["Component" "# Amount" "$ Price"])

(defn portfolio-list-column [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/h4
        #js {:className   "portfolio-list-column col-sm-2"}
        (dom/div nil app)))))

(defn portfolio-list-columns [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "portfolio-list-columns"}
        (apply dom/div #js {:className "row"}
          (om/build-all
            portfolio-list-column
            (map (fn [elem] elem) columns)))))))


(defn portfolio-list-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan       (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)
            remove-chan      (sub (:notif-chan (om/get-shared owner)) :remove-click (chan) false)]

        (go-loop []
          (let [[v c]            (alts! [remove-chan click-chan]
                                   {:as {:default true}})
                add-component    (fn []
                                   (if (some #{(:value v)} @app)
                                     @app
                                     (conj @app (:value v))))

                remove-component (fn []
                                   (remove (fn [elem] (= elem (:value v))) @app))]

            (condp = c
              remove-chan   (om/transact! app remove-component)
              click-chan    (om/transact! app add-component)
              :default      nil))
            (recur))))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "portfolio-container"}
        (om/build portfolio-list-columns true)
        (apply dom/div #js {:className "list-group portfolio-list"}
          (om/build-all
            component-row/portfolio-component-view
            (map
              (fn [elem] elem)
              app)))))))
