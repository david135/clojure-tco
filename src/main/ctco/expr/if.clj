;;----------------------------------------------------------------------
;; File if.clj
;; Written by Chris Frisz
;; 
;; Created 30 Mar 2012
;; Last modified 22 Apr 2012
;; 
;; Defines the If record (triv, srs, and cps variants) for the Clojure
;; TCO compiler.
;;----------------------------------------------------------------------

(ns ctco.expr.if
  (:require [ctco.protocol
             [pabstract-k :as pabs-k]
             [pemit :as pemit]
             [pcps-triv :as triv]
             [pcps-srs :as srs]
             [pthunkify :as pthunkify]
             [pwalkable :as pwalkable]]
            [ctco.expr.cont :as cont]
            [ctco.util.new-var :as new-var])
  (:import [ctco.expr.cont
            Cont AppCont]))

(defrecord IfCps [test conseq alt]
  pabs-k/PAbstractK
    (abstract-k [this app-k]
      (let [ctor #(IfCps. %1 %2 %3)]
        (pwalkable/walk-expr this #(pabs-k/abstract-k % app-k) ctor)))
  
  pthunkify/PThunkify
    (thunkify [this]
      (let [ctor #(IfCps. %1 %2 %3)]
        (pwalkable/walk-expr this pthunkify/thunkify ctor))))

(defrecord IfTriv [test conseq alt]
  triv/PCpsTriv
  (cps [this]
    (let [ctor #(IfCps. %1 %2 %3)]
      (pwalkable/walk-expr this triv/cps ctor)))

  pthunkify/PThunkify
    (thunkify [this]
      (let [ctor #(IfTriv. %1 %2 %3)]
        (pwalkable/walk-expr this pthunkify/thunkify ctor))))

(defrecord IfSrs [test conseq alt]
  srs/PCpsSrs
    (cps [this k]
      (letfn [(cps-if [expr]
                (condp extends? (type expr)
                  triv/PCpsTriv (let [EXPR (triv/cps expr)]
                                  (AppCont. k EXPR))
                  srs/PCpsSrs (srs/cps expr k)))]
        (let [test (:test this)
              CONSEQ (cps-if (:conseq this))
              ALT (cps-if (:alt this))]
          (if (extends? triv/PCpsTriv (type test))
              (let [TEST (triv/cps test)]
                (IfCps. TEST CONSEQ ALT))
              (let [s (new-var/new-var 's)
                    K-body (IfCps. s CONSEQ ALT)
                    K (Cont. s K-body)]
                (srs/cps test K))))))

  pthunkify/PThunkify
    (thunkify [this]
      (let [ctor #(IfSrs. %1 %2 %3)]
        (pwalkable/walk-expr this pthunkify/thunkify ctor))))

(def if-emit
  {:emit (fn [this]
           (let [test (pemit/emit (:test this))
                 conseq (pemit/emit (:conseq this))
                 alt (pemit/emit (:alt this))]
             `(if ~test ~conseq ~alt)))})

(def if-walkable
  {:walk-expr (fn [this f ctor]
                (let [TEST (f (:test this))
                      CONSEQ (f (:conseq this))
                      ALT (f (:alt this))]
                  (ctor TEST CONSEQ ALT)))})

(extend IfCps
  pemit/PEmit
    if-emit

  pwalkable/PWalkable
    if-walkable)

(extend IfSrs
  pemit/PEmit
    if-emit)

(extend IfTriv
  pemit/PEmit
    if-emit

  pwalkable/PWalkable
    if-walkable)

