(defproject recipe-recommendation-system "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [midje "1.10.10"]
                 [com.github.seancorfield/next.jdbc "1.3.955"]
                 [mysql/mysql-connector-java "8.0.30"]
                 [criterium "0.4.6"]
                 [com.clojure-goes-fast/clj-memory-meter "0.3.0"]
                 [com.clojure-goes-fast/clj-java-decompiler "0.3.6"]]
  :repl-options {:init-ns recipe-recommendation-system.core}
  :jvm-opts ["-Djdk.attach.allowAttachSelf"]
  :main recipe-recommendation-system.core)
