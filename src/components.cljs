(ns portfolio.components
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.util :as util]
    [portfolio.component-row :as component-row]
    [portfolio.search-results :as search]
    [portfolio.slider :as slider])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def columns ["Component" "# Amount" "$ Price" "$ Total Position" "Chart"])

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
    (render-state [this state]
      (dom/div #js {:className "portfolio-container"}
        (om/build portfolio-list-columns true)
          (apply dom/div #js {:className "list-group portfolio-list"}
               (interleave
                 (om/build-all
                   component-row/portfolio-component-view
                   (map
                     (fn [elem] {:name   (first elem)
                                 :amount (second elem)
                                 :prices  (get-in app [:data (first elem) :prices])})
                   (:components app)))
                 (om/build-all
                   search/result-separator-view
                   (repeat (count (:components app)) nil))))))))
