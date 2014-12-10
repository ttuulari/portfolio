(ns portfolio.navbar
  (:require
    [om.core :as om :include-macros true]
    [om-bootstrap.button :as b]
    [om-bootstrap.nav :as n]
    [om-tools.dom :as d :include-macros true]))

(defn navbar-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (d/div {:class "navbar navbar-info"}
             (d/div {:class "navbar-header"}
                    (d/b {:type "button"
                          :class "navbar-toggle"
                          :data-toggle "collapse"
                          :data-target ".navbar-responsive-collapse"})
                    (d/a {:class "navbar-brand"} "Portfol.io"))
             (d/div {:class "navbar-collapse collapse navbar-responsive-collapse"}
                    (d/ul {:class "nav navbar-nav"}
                          (d/li {:className "dropdown"}
                                (d/a {:class "dropdown-toggle"
                                      :data-toggle "dropdown"}
                                     (d/b {:class "mdi-navigation-menu"})))))))))


