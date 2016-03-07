(ns captcha.core
  (:use [anglican [core :exclude [-main]] runtime emit
         [state :only [get-predicts get-log-weight set-log-weight]]])
  (:import [Captcha] [Imshow] [Utils])
  (:require [clojure.core.matrix :as m]
            [clojure.core.matrix.operators :as op]
            [clojure.core.matrix.linear :as linalg]
            [clojure.core.matrix.stats :as stats])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
(import java.awt.Toolkit)
(import java.io.PrintWriter)
(import java.util.Random)




;; ;; (def alphabet "ABCDEFGH/IJKLMNOPQRSTUVWXXYZ                             ")
;; (def alphabet "ABCDEFGHIJKLMNOPQRSTUVWXXYZabcdefghijklmnopqrstuvwxyz")
;; (def new-dim-size 300) ; 700 300 LAST THING CHANGED
;; (def SHRINK_LENGTH 35)
;; (def SHRINK_WIDTH 75)
;; (def LENGTH 70)
;; (def WIDTH 150)
;; (def total-pixels (* LENGTH WIDTH))

;; ;; define some constants and functions
;; (def join clojure.string/join)
;; (def split clojure.string/split)
;; (def eye m/identity-matrix)
;; (defn scalar-multiply [a b] (op/* a b))
;; (def generateBaseline #(Captcha/generateBaseline %))
;; (def makeCaptcha #(Captcha. %1 %2))
;; (def dot m/dot)
;; (def hypot m/distance)

;; (def drawLetter #(.drawLetter %1 %2 %3 %4 %5 %6 %7 %8 %9 %10))
;; (def globalBlur #(.globalBlur %1 %2))
;; (def getPixels #(.getPixels %))
;; (def clear #(.clear %))
;; (defn shrink [c] (.resize c SHRINK_LENGTH SHRINK_WIDTH))
;; (defn resize [c a b] (.resize c a b))
;; (defn drawLine [c x1 y1 x2 y2] (.drawLine c x1 y1 x2 y2))
;; (defn blurBaseline [c r] (.blurBaseline c r))
;; (defn equalizeHist [c] (.equalizeHist c))
;; (def drawText #(.drawText %1 %2 %3 %4 %5 %6 %7 %8))
;; (def ripple #(.ripple %1 %2 %3 %4))
;; (def dilate #(.dilate %))

;; ; generate random matrix for feature reduction
;; (def mat (m/new-sparse-array [new-dim-size (* SHRINK_LENGTH SHRINK_WIDTH)]))
;; (defn create-sparse-mat []
;;   (loop [i 0]
;;     (if (= i new-dim-size) 1
;;       (do (loop [j 0]
;;         (if (= j (* SHRINK_LENGTH SHRINK_WIDTH)) 1
;;           (let [is-zero (sample (flip (- 1 (/ 1 (sqrt (* SHRINK_LENGTH SHRINK_WIDTH))))))]
;;                 (if is-zero
;;                   (m/mset! mat i j 0)
;;                   (if (sample (flip 0.5))
;;                     (m/mset! mat i j 1)
;;                     (m/mset! mat i j -1)))
;;            (recur (inc j)))
;;           ))
;;         (recur (inc i))))))
;; (create-sparse-mat)
;; (defn reduce-dim [v] (m/mmul mat (m/array v)))

;; (defn sample-char []
;;   {:is-present (sample (flip 0.5))
;;    :letter (str (nth alphabet (sample (uniform-discrete 0 (count alphabet)))))
;;    :x (sample (uniform-discrete 0 WIDTH))
;;    :y (sample (uniform-discrete 0 LENGTH))
;;    :theta 0
;;    :scale (sample (uniform-discrete 2 4))
;;    :thickness (sample (uniform-discrete 2 4))
;;    :sigma (* 6 (sample (beta 1 2)))})

;; (defn save-weights [filename w]
;;   (do
;;     (spit filename "")
;;     (spit filename (join "\n" w) :append true) w))



;; (defm sample-point [lower-x upper-x lower-y upper-y]
;;   {:x (sample (uniform-discrete lower-x upper-x))
;;    :y (sample (uniform-discrete lower-y upper-y))})


;; (defn outside-neighborhood [point nums r]
;;   (not (some #(<= (sqrt (+ (pow (- (:x point) (:x %)) 2)
;;                            (pow (- (:y point) (:y %)) 2)))
;;                   r)
;;              nums)))

;; (defn makeRandom [] (Random.))
;; (defn makeRandomSeed [seed] (Random. seed))
;; (defn nextInt [rdm bound] (.nextInt rdm bound))

;; (with-primitive-procedures [makeRandom makeRandomSeed outside-neighborhood nextInt]
;; (defm get-rand-points [is-present list-length lower-x upper-x lower-y upper-y r]
;;   (let [rdm (makeRandom)]
;;     (loop [nums []]
;;       (if (= (count nums) list-length)
;;         nums
;;         (let [testPoint (sample-point lower-x upper-x lower-y upper-y)
;;               next-gen (makeRandomSeed (:x testPoint))
;;               keepPoint (if (or (not (nth is-present (count nums)))
;;                                 (outside-neighborhood testPoint nums r))
;;                           testPoint
;;                           (loop [replacement {:x (+ (nextInt next-gen (- upper-x lower-x)) lower-x)
;;                                               :y (+ (nextInt next-gen (- upper-y lower-y)) lower-y)}]
;;                             (if (outside-neighborhood replacement nums r)
;;                               replacement
;;                               (recur {:x (nextInt next-gen upper-x)
;;                                       :y (nextInt next-gen upper-y)}))))]
;;           (recur (conj nums keepPoint)))))))
;;   )


;; (def rendered-captcha (makeCaptcha LENGTH WIDTH))
;; (defn renderLetter [canvas letter]
;;   (.drawLetter canvas (:letter letter) (:x letter) (:y letter) (:scale letter) (:theta letter)
;;                (:thickness letter) (:sigma letter) (:is-present letter)))


;; (defn get-coords [n limit] (repeatedly n #(sample (uniform-discrete 0 limit))))


;; (with-primitive-procedures [eye join generateBaseline makeCaptcha split dot hypot get-coords equalizeHist ripple
;;                             drawLetter globalBlur getPixels reduce-dim scalar-multiply clear shrink blurBaseline
;;                             nextInt makeRandom makeRandomSeed outside-neighborhood nextInt drawLine drawText dilate]
;; (defquery guess-captcha [baseline-captcha]
;;   (let [num-letters (sample (uniform-discrete 4 12)) ;7
;;         is-present (repeatedly num-letters #(sample (flip 0.5)))
;; ;;         is-present (repeat num-letters true)

;;         letters (repeatedly num-letters #(str (nth alphabet (sample (uniform-discrete 0 (count alphabet))))))
;; ;;         text (join letters)

;;         x-pos (repeatedly num-letters #(sample (uniform-discrete 0 WIDTH)))
;;         y-pos (repeat num-letters (sample (uniform-discrete 0 LENGTH)))

;; ;;         points (get-rand-points is-present num-letters 15 (- WIDTH 15) 15 (- LENGTH 15) 15)
;; ;;         x-pos (map #(:x %) points)
;; ;;         y-pos (map #(:y %) points)

;; ;;         x-pos (sample (uniform-discrete 0 WIDTH))
;; ;;         y-pos (sample (uniform-discrete 0 LENGTH))
;; ;;         spacing (sample (uniform-continuous 0.7 0.9))

;; ;;         thetas 0
;; ;;         thickness (sample (uniform-discrete 2 5))
;; ;;         scale (sample (uniform-discrete 1 4))

;;         amplitude (sample (uniform-discrete 0 12))
;;         period (sample (uniform-continuous 190 500))
;;         shift (sample (uniform-discrete 0 period))

;;         scale (repeatedly num-letters #(sample (uniform-discrete 2 4)))

;;         color (repeat num-letters 255)

;;         do-dilate true ;(sample (flip 0.5))

;;         thetas (repeat num-letters 0)
;; ;;         thetas (repeatedly num-letters #(sample (uniform-continuous -20 20))) ;-30 30

;;         thickness (repeatedly num-letters #(sample (uniform-discrete 2 6)))

;;         letter-sigmas (repeatedly num-letters #(sample (uniform-continuous 0 2)))
;; ;;         letter-sigmas (repeatedly num-letters #(* 7 (sample (beta 1 2))))
;; ;;         letter-sigmas (repeat num-letters 0.1)


;; ;;         rendered-sigma 0.8
;; ;;         baseline-sigma 0.0
;;         rendered-sigma (sample (uniform-continuous 0.4 2)) ;1.4;(* 8 (sample (beta 1 2)))
;;         baseline-sigma (sample (uniform-continuous 1 2)) ;1.2;(* 8 (sample (beta 1 2)))
;; ;;         baseline-sigma 0.01
;;         ]
;;     (clear rendered-captcha)

;; ;;     (drawText rendered-captcha text x-pos y-pos scale thickness spacing rendered-sigma)
;;     (doall (map #(drawLetter rendered-captcha %1 %2 %3 %4 %5 %6 %7 %8 %9)
;;                 letters x-pos y-pos scale thetas thickness letter-sigmas is-present color))
;;     (globalBlur rendered-captcha rendered-sigma)
;;     (blurBaseline baseline-captcha baseline-sigma)
;;     (ripple rendered-captcha amplitude period shift)

;; ;;     (if do-dilate (dilate rendered-captcha))

;;     (shrink rendered-captcha)
;;     (shrink baseline-captcha)

;;     (let [rendered-pixels (reduce-dim (vec (getPixels rendered-captcha)))
;;           baseline-pixels (reduce-dim (vec (getPixels baseline-captcha)))
;;           std (* 100 (sample (uniform-continuous 9 20))) ; 100 7 25
;;           error  (hypot rendered-pixels baseline-pixels)]
;;       (doall (map #(observe (normal %1 std) %2) rendered-pixels baseline-pixels)) ;2500 ; 4066
;;       (observe (flip 1) (> (min (apply min x-pos) (apply min y-pos)) 10))

;;       (predict :text letters)
;;       (predict :x x-pos)
;;       (predict :y y-pos)
;;       (predict :scale scale)
;;       (predict :theta thetas)
;;       (predict :thickness thickness)
;;       (predict :sigma letter-sigmas)
;;       (predict :amplitude amplitude)
;;       (predict :period period)
;;       (predict :shift shift)
;; ;;       (predict :spacing spacing)
;;       (predict :color color)
;;       (predict :is-present is-present)
;;       (predict :error error)
;; ))))


;; (def baseline (Captcha/generateBaseline 7))
;; ;; (def baseline (Captcha. LENGTH WIDTH "captcha4.png"))
;; ;; (drawText baseline "boron" 10 40 3 2 0.8 0)
;; ;; (ripple baseline 10 200 10)


;; ;; (.saveImg baseline)

;; (.denoise baseline)

;; ;; (.dilate baseline)
;; (globalBlur baseline 1)
;; (.showImg baseline)



;; ;; (def stime (System/currentTimeMillis))
;; ;; (def sampler (doquery :rmh guess-captcha [baseline] :alpha 1 :sigma 4)) ; 0.8 3.3
;; ;; (def sample-rate 1000)
;; ;; (def max-runs 60000)
;; ;; (loop [num-tries 1] ; can also append likelihoods
;; ;;   (if (> num-tries  (* 1 max-runs))
;; ;;     1
;; ;;     (let [c (get-predicts (first (drop num-tries sampler)))
;; ;;           captcha (Captcha. LENGTH WIDTH)]
;; ;; ;;          (drawText captcha (:text c) (:x c) (:y c) (:scale c) (:thickness c) (:spacing c) (:sigma c))
;; ;;             (doall (map #(drawLetter captcha %1 %2 %3 %4 %5 %6 %7 %8 %9)
;; ;;                   (:text c) (:x c) (:y c) (:scale c) (:theta c) (:thickness c) (:sigma c) (:is-present c) (:color c)))
;; ;;         (ripple captcha (:amplitude c) (:period c) (:shift c))
;; ;;       (.saveImg captcha)

;; ;;     (recur (+ num-tries sample-rate)))))

;; ;; (.beep (Toolkit/getDefaultToolkit))
;; ;; (float (/ (- (System/currentTimeMillis) stime) 60000))




;; (def likelihoods (map get-log-weight (take max-runs sampler)))
;; (save-weights "weights.txt" likelihoods)



;; (def predicts (map get-predicts (take max-runs sampler)))
;; (apply min-key :error predicts)

