(defproject dm5-core "0.1.0-SNAPSHOT"
  :description "DeepaMehta 5 Core Prototype"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.datomic/datomic-pro "0.9.5407"]]
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns dm5.core
                 :init (install-schema "resources/dm5-core-schema.edn")}
)
