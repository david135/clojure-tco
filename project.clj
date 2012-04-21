;;----------------------------------------------------------------------
;; File project.clj
;; Written by Chris Frisz
;; 
;; Created  4 Feb 2012
;; Last modified 21 Apr 2012
;; 
;; Project declaration for clojure-tco. 
;;----------------------------------------------------------------------

(defproject bbc "0.2.0"
  :description "Adding proper tail calls to Clojure."
  :url "https://github.iu.edu/cjfrisz/clojure-tco.git"
  :dependencies [[org.clojure/clojure
                  "1.3.0"]
                 [org.clojure/core.match
                  "0.2.0-alpha9"]]
  :dev-dependencies [[swank-clojure
                      "1.4.2"]]
  :plugins [[lein-swank "1.4.3"]]
  :source-path "src/main"
  :test-path "src/test"
  :repl-init bbc)
