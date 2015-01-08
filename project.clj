(defproject portfolio "0.1.0-SNAPSHOT"
  :description "Example ClojureScript/Om portfolio analysis application."
  :url "http://example.com/FIXME"

  :repositories {"sonatype-staging"
                 "https://oss.sonatype.org/content/groups/staging/"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2496"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [racehub/om-bootstrap "0.3.2"]
                 [om "0.7.3"]
                 [jayq "2.5.2"]
                 [prismatic/dommy "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [com.cemerick/clojurescript.test "0.3.3"]]

  :source-paths ["src"]

  :cljsbuild {

    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :output-to "portfolio.js"
                :output-dir "out/dev"
                :optimizations :none
                :source-map true}}

             {:id "test"
              :source-paths ["bower_components/react"
                             "bower_components/react-externs"
                             "bower_components/externs-jquery"
                             "bower_components/jquery/dist"
                             "src"
                             "test"]
              :compiler {:pretty-print true
                         :output-dir "out/test"
                         :output-to "out/test/unit-test.js"
                         :optimizations :whitespace
                         :preamble ["react.min.js"
                                    "jquery.min.js"]
                         :externs ["externs.js"
                                   "index.js"]}
              }

             {:id "prod"
              :source-paths ["bower_components/react"
                             "bower_components/react-externs"
                             "bower_components/jquery/dist"
                             "bower_components/externs-jquery"
                             "bower_components/chartist/dist"
                             "bower_components/nouislider/distribute"
                             "src"]
              :compiler {:pretty-print true
                         :output-dir "out/prod"
                         :output-to "out/prod/portfolio.js"
                         :optimizations :advanced
                         :externs ["externs.js"
                                   "index.js"
                                   "chartist.js"
                                   "jquery.nouislider.all.min.js"]}
              }]

    :test-commands {"unit-tests" ["slimerjs" :runner
                                  "window.literal_js_was_evaluated=true"
                                  "bower_components/es5-shim/es5-shim.js"
                                  "bower_components/es5-shim/es5-sham.js"
                                  "bower_components/console-polyfill/index.js"
                                  "out/test/unit-test.js"]}
})
