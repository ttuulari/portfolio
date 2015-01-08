(ns portfolio.core
  "portfolio.core contains the app root component."
  (:require
   [cljs.core.async :as async
    :refer [<! >! chan pub sub put! timeout]]
   [portfolio.range-buttons :as range-buttons]
   [portfolio.graph :as graph]
   [portfolio.container :as container]
   [portfolio.prices :as prices]
   [clojure.data :as data]
   [om.core :as om :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def range-data (range-buttons/update-range "2014-05-30" -30))

(def app-state (atom {:selected-date    range-data
                      :results          []
                      :components       {}
                      :data             prices/prices}))

(def search-chan (chan))
(def notif-chan  (pub search-chan :topic))

(defn components-view [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (container/render-container app))))

(om/root
  components-view
  app-state
  {:target (. js/document (getElementById "components"))
   :shared {:search-chan   search-chan
            :notif-chan    notif-chan}})

;================ Time Travel =================

(def app-history (atom [@app-state]))

(defn store-undo [app-history o n]
  (let [[_ new _] (data/diff o n)]
    (when-not (= (last @app-history) n)
        (swap! app-history conj n))))

(add-watch app-state :history
  (fn [_ _ o n]
    (store-undo app-history o n)))

(defn undo []
   (when (> (count @app-history) 1)
      (swap! app-history pop)
      (reset! app-state (last @app-history))))

(defn receive-undos []
  (let [undo-chan (sub notif-chan :undo (chan) false)]
    (go-loop
     []
     (let [undo-elem      (<! undo-chan)]
       (undo))
     (recur))))

(receive-undos)

;================ App state demo ==============
;app-state
;(swap! app-state
;       (fn [a] (assoc @app-state :results [])))
