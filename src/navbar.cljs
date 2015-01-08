(ns portfolio.navbar
  (:require
    [om.core :as om :include-macros true]
    [om-bootstrap.button :as b]
    [om-bootstrap.nav :as n]
    [portfolio.util :as util]
    [cljs.core.async :as async
      :refer [put!]]
    [om-tools.dom :as d :include-macros true]))

(defn undo-click [owner]
   (let [search-chan   (:search-chan  (om/get-shared owner))]
     (put! search-chan
           {:topic   :undo})))

(defn navbar-view [app owner]
  (reify
    om/IRenderState
    (render-state
     [this state]
     (d/div {:class "navbar navbar-material-bluegrey"}
            (d/div {:class "navbar-header"}
                   (d/b {:type "button"
                         :class "navbar-toggle"
                         :data-toggle "collapse"
                         :data-target ".navbar-responsive-collapse"})
                   (d/a {:class "navbar-brand"}
                        "Portfol.io"))
            (d/div {:class "navbar-collapse collapse navbar-responsive-collapse"}
                   (d/ul {:class "nav navbar-nav"}
                         (d/li {::class "dropdown"}
                               (d/a {:class "dropdown-toggle"
                                     :data-toggle "dropdown"}
                                    (d/b {:class "mdi-navigation-menu"}))))
                   (d/ul {:class "nav navbar-nav navbar-right"}
                         (d/li {}
                               (d/a {:class "btn-material-indigo"
                                     :on-click (fn []
                                                 (undo-click owner))}
                                    "Undo"))))))))
