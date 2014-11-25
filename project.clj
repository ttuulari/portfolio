(defproject portfolio "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :repositories {"sonatype-staging"
                 "https://oss.sonatype.org/content/groups/staging/"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.7.1"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "portfolio"
              :source-paths ["src"]
              :compiler {
                :output-to "portfolio.js"
                :output-dir "out"
                :optimizations :none
                :externs ["lib/lib.js"
                          "lib/flotr2.js"
                          ]
                :source-map true}}]})
