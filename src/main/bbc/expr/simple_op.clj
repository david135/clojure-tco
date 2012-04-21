;;----------------------------------------------------------------------
;; File simple_op.clj
;; Written by Chris Frisz
;; 
;; Created  2 Apr 2012
;; Last modified 21 Apr 2012
;; 
;; Defines the SimpleOp record types for the Clojure TCO compiler.
;;----------------------------------------------------------------------

(ns bbc.expr.simple-op
  (:require [bbc.protocol
             [pabstract-k :as pabs-k]
             [pemit :as pemit]
             [pcps-srs :as srs]
             [pcps-triv :as triv]
             [pthunkify :as pthunkify]
             [pwalkable :as pwalkable]]
            [bbc.expr.cont :as cont]
            [bbc.util.new-var :as new-var])
  (:import [bbc.expr.cont
            Cont AppCont]))

(defrecord SimpleOpCps [op opnd*]
  pabs-k/PAbstractK
    (abstract-k [this app-k]
      (let [ctor #(SimpleOpCps. %1 %2)]
        (pwalkable/walk-expr this #(pabs-k/abstract-k % app-k) ctor)))

  pthunkify/PThunkify
    (thunkify [this]
      (let [ctor #(SimpleOpCps. %1 %2)]
        (pwalkable/walk-expr this pthunkify/thunkify ctor))))

(defrecord SimpleOpTriv [op opnd*]
  triv/PCpsTriv
    (cps [this]
      (let [ctor #(SimpleOpCps. %1 %2)]
        (pwalkable/walk-expr this triv/cps ctor)))

  pthunkify/PThunkify
    (thunkify [this]
      (let [ctor #(SimpleOpTriv. %1 %2)]
        (pwalkable/walk-expr this pthunkify/thunkify ctor))))

(defrecord SimpleOpSrs [op opnd*]
  srs/PCpsSrs
    (cps [this k]
      (letfn [(cps-op [pre-opnd* post-opnd* k]
                (if (nil? (seq pre-opnd*))
                    (let [op (SimpleOpCps. (:op this) post-opnd*)]
                      (AppCont. k op))
                    (let [fst (first pre-opnd*)
                          rst (rest pre-opnd*)]
                      (if (extends? triv/PCpsTriv (type fst))
                          (let [FST (triv/cps fst)
                                POST-OPND* (conj post-opnd* FST)]
                            (recur rst POST-OPND* k))
                          (let [s (new-var/new-var 's)
                                POST-OPND* (conj post-opnd* s)
                                RST (cps-op rst POST-OPND* k)
                                K (Cont. s RST)]
                            (srs/cps fst K))))))]
        (cps-op (:opnd* this) [] k)))

  pthunkify/PThunkify
    (thunkify [this]
      (let [ctor #(SimpleOpSrs. %1 %2)]
        (pwalkable/walk-expr this pthunkify/thunkify ctor))))

(def simple-op-emit
  {:emit (fn [this]
           (let [op (:op this)
                 opnd* (map pemit/emit (:opnd* this))]
             `(~op ~@opnd*)))})

(def simple-op-walk
  {:walk-expr (fn [this f ctor]
                (let [OPND* (vec (map #(f %) (:opnd* this)))]
                  (ctor (:op this) OPND*)))})

(extend SimpleOpCps
  pemit/PEmit
    simple-op-emit
  
  pwalkable/PWalkable
    simple-op-walk)

(extend SimpleOpSrs
  pemit/PEmit
    simple-op-emit
  
  pwalkable/PWalkable
    simple-op-walk)

(extend SimpleOpTriv
  pemit/PEmit
    simple-op-emit
  
  pwalkable/PWalkable
    simple-op-walk)