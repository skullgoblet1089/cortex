(ns cortex.impl.default
  "Default implementations for coretx protocols."
  (:require [cortex.protocols :as cp])
  (:require [clojure.core.matrix :as m])
  (:require [cortex.util :as util :refer [error EMPTY-VECTOR]]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;; default to assuming zero parameters
(extend-protocol cp/PParameters
  Object
	  (parameters 
      ([m]
        ;; default to assuming zero parameters
        EMPTY-VECTOR))
    (update-parameters 
      ([m parameters]
        (when (> 0 (long (m/ecount parameters))) (error "Non-zero length for parameter update"))
        m)))

;; default gradient function assumes zero parameters
(extend-protocol cp/PGradient
  Object
    (gradient 
      ([m]
        EMPTY-VECTOR)))

;; default parameter count implementation is to... err... count the parameters. duh!
(extend-protocol cp/PParameterCount
  Object
    (parameter-count 
      ([m]
        (m/ecount (cp/parameters m)))))

;; Default loss gradient function returns :loss-gradient-fn (may be nil)
(extend-protocol cp/PLossGradientFunction
  Object
    (loss-gradient-fn 
      ([m]
        (:loss-gradient-fn m))))

;; default training implementation is to:
;; 1. Run forward pass
;; 2. Gets the loss gradient function for the module (or defaults to MSE)
;; 3. Compute outpout gradient
;; 4. Run backward pass
(extend-protocol cp/PTraining
  Object
    (train 
      ([m input target]
        (let [m (cp/forward m input)
              output (cp/output m)
              loss-function (or (cp/loss-gradient-fn m) util/mse-gradient-fn) ;; default to MSE
              output-gradient (loss-function output target)
              m (cp/backward m input output-gradient)]
          m))))




