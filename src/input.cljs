(ns portfolio.input
  (:require
    [cljs.core.async :as async
      :refer [>! put! sub chan]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn submit [owner]
  (let [value         (.trim (.-value (om/get-node owner "term")))
        search-chan   (:search-chan  (om/get-shared owner))]
    (put! search-chan
          {:topic :search
           :value value})
    false))

(defn input-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)]
        (go-loop []
          (let [search-elem      (<! click-chan)]
            (set! (.-value (om/get-node owner "term")) ""))
          (recur))))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "form-group search-container"}
        (dom/form #js
          {
            :className "navbar-form"
            :role: "search"
            :onSubmit (fn [] (submit owner))
            :onChange (fn [] (submit owner))}
        (dom/input #js {:type "text"
                        :autoComplete "off"
                        :className "form-control floating-label"
                        :placeholder "Instrument"
                        :data-hint "Start typing instrument name"
                        :name "srch-term"
                        :id "srch-term"
                        :ref "term"}))))))
