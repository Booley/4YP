(ns captcha.core
  (:use [anglican [core :exclude [-main]] runtime emit
         [state :only [get-predicts get-log-weight set-log-weight]]])
  (:import [Captcha] [Imshow] [Utils] [CNN])
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

;; (import org.bytedeco.javacpp.opencv_imgproc)

(def net (CNN. "/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/models/captchanet_deploy.prototxt" "/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/models/snapshots/captchanet_iter_100.caffemodel"))

;; (def a (Captcha/generateBaseline 1))
;; (vec (.forward net a))

(def deploy-path "/Users/bomoon/Documents/4YP/CaptchaTools/models/")
(def wave-net (CNN. "/Users/bomoon/Documents/4YP/CaptchaTools/models/wave_net_deploy.prototxt"
                    "/Users/bomoon/Documents/4YP/CaptchaTools/models/snapshots/wave_net_weights.caffemodel") )
(def letter-net (CNN. "/Users/bomoon/Documents/4YP/CaptchaTools/models/letter_net_deploy.prototxt"
                    "/Users/bomoon/Documents/4YP/CaptchaTools/models/snapshots/letter_net_weights.caffemodel") )
(def position-net (CNN. "/Users/bomoon/Documents/4YP/CaptchaTools/models/position_net_deploy.prototxt"
                    "/Users/bomoon/Documents/4YP/CaptchaTools/models/snapshots/position_net_weights.caffemodel") )
(def num-net (CNN. "/Users/bomoon/Documents/4YP/CaptchaTools/models/num_net_deploy.prototxt"
                    "/Users/bomoon/Documents/4YP/CaptchaTools/models/snapshots/num_net_weights.caffemodel") )
(def forward #(.forward %1 %2))

;; (def alphabet "ABCDEFGH/IJKLMNOPQRSTUVWXXYZ                             ")
(def alphabet "ABCDEFGHIJKLMNOPQRSTUVWXXYZabcdefghijklmnopqrstuvwxyz")
(def new-dim-size 300) ; 700 300 LAST THING CHANGED
(def SHRINK_LENGTH 35)
(def SHRINK_WIDTH 75)
(def LENGTH 70)
(def WIDTH 150)
(def total-pixels (* LENGTH WIDTH))

;; define some constants and functions
(def join clojure.string/join)
(def split clojure.string/split)
(def eye m/identity-matrix)
(defn scalar-multiply [a b] (op/* a b))
(def generateBaseline #(Captcha/generateBaseline %))
(def makeCaptcha #(Captcha. %1 %2))
(def dot m/dot)
(def hypot m/distance)

(def drawLetter #(.drawLetter %1 %2 %3 %4 %5 %6 %7 %8 %9 %10))
(def globalBlur #(.globalBlur %1 %2))
(def getPixels #(.getPixels %))
(def clear #(.clear %))
(defn shrink [c] (.resize c SHRINK_LENGTH SHRINK_WIDTH))
(defn resize [c a b] (.resize c a b))
(defn drawLine [c x1 y1 x2 y2] (.drawLine c x1 y1 x2 y2))
(defn blurBaseline [c r] (.blurBaseline c r))
(defn equalizeHist [c] (.equalizeHist c))
(def drawText #(.drawText %1 %2 %3 %4 %5 %6 %7 %8))
(def ripple #(.ripple %1 %2 %3 %4))
(def dilate #(.dilate %))

(defn render-predicts [c]
  (let [captcha (Captcha. LENGTH WIDTH)]
    (doall (map #(drawLetter captcha %1 %2 (:y c) %3 0 %4 %5 true 255)
                  (:text c) (:x c) (:scale c) (:thickness c) (:sigma c)))
    (ripple captcha (:amplitude c) (:period c) (:shift c))
    (.saveImg captcha)))

; generate random matrix for feature reduction
(def mat (m/new-sparse-array [new-dim-size (* SHRINK_LENGTH SHRINK_WIDTH)]))
(defn create-sparse-mat []
  (loop [i 0]
    (if (= i new-dim-size) 1
      (do (loop [j 0]
        (if (= j (* SHRINK_LENGTH SHRINK_WIDTH)) 1
          (let [is-zero (sample (flip (- 1 (/ 1 (sqrt (* SHRINK_LENGTH SHRINK_WIDTH))))))]
                (if is-zero
                  (m/mset! mat i j 0)
                  (if (sample (flip 0.5))
                    (m/mset! mat i j 1)
                    (m/mset! mat i j -1)))
           (recur (inc j)))
          ))
        (recur (inc i))))))
(create-sparse-mat)
(defn reduce-dim [v] (m/mmul mat (m/array v)))

(defn sample-char []
  {:is-present (sample (flip 0.5))
   :letter (str (nth alphabet (sample (uniform-discrete 0 (count alphabet)))))
   :x (sample (uniform-discrete 0 WIDTH))
   :y (sample (uniform-discrete 0 LENGTH))
   :theta 0
   :scale (sample (uniform-discrete 2 4))
   :thickness (sample (uniform-discrete 2 4))
   :sigma (* 6 (sample (beta 1 2)))})

(defn save-weights [filename w]
  (do
    (spit filename "")
    (spit filename (join "\n" w) :append true) w))



(defm sample-point [lower-x upper-x lower-y upper-y]
  {:x (sample (uniform-discrete lower-x upper-x))
   :y (sample (uniform-discrete lower-y upper-y))})


(defn outside-neighborhood [point nums r]
  (not (some #(<= (sqrt (+ (pow (- (:x point) (:x %)) 2)
                           (pow (- (:y point) (:y %)) 2)))
                  r)
             nums)))

(defn makeRandom [] (Random.))
(defn makeRandomSeed [seed] (Random. seed))
(defn nextInt [rdm bound] (.nextInt rdm bound))

(with-primitive-procedures [makeRandom makeRandomSeed outside-neighborhood nextInt]
(defm get-rand-points [is-present list-length lower-x upper-x lower-y upper-y r]
  (let [rdm (makeRandom)]
    (loop [nums []]
      (if (= (count nums) list-length)
        nums
        (let [testPoint (sample-point lower-x upper-x lower-y upper-y)
              next-gen (makeRandomSeed (:x testPoint))
              keepPoint (if (or (not (nth is-present (count nums)))
                                (outside-neighborhood testPoint nums r))
                          testPoint
                          (loop [replacement {:x (+ (nextInt next-gen (- upper-x lower-x)) lower-x)
                                              :y (+ (nextInt next-gen (- upper-y lower-y)) lower-y)}]
                            (if (outside-neighborhood replacement nums r)
                              replacement
                              (recur {:x (nextInt next-gen upper-x)
                                      :y (nextInt next-gen upper-y)}))))]
          (recur (conj nums keepPoint)))))))
  )


(def rendered-captcha (makeCaptcha LENGTH WIDTH))
(defn renderLetter [canvas letter]
  (.drawLetter canvas (:letter letter) (:x letter) (:y letter) (:scale letter) (:theta letter)
               (:thickness letter) (:sigma letter) (:is-present letter)))


(defn get-coords [n limit] (repeatedly n #(sample (uniform-discrete 0 limit))))



;; neural net wrappers

;; returns probabilities for classes 3-9
(defn predict-num-letters [captcha]
  (let [weights (do (.forward num-net captcha)
                    (.normalize num-net))]
    (vec weights)
))

(with-primitive-procedures [forward]
(defm get-net-output [c]
  (let [num-letters (+ 3 (adaptive-sample (uniform-discrete 0 7)
                           discrete
                           #(let [weights (do (.forward num-net %) (.normalize num-net))] [(vec weights)])
                           identity
                           c))
        wave-net-output (let [weights (vec (forward wave-net c))]
                          {:amplitude (max 0 (first weights)),
                           :period (max 0 (second weights)),
                           :shift (max 0 (nth weights 2))})
        amplitude (adaptive-sample (uniform-discrete 0 12)
                     normal
                     #(let [mu (:amplitude wave-net-output) std 20 _ %] [mu std])
                     identity
                     nil)
        period (adaptive-sample (uniform-discrete 190 500)
                     normal
                     #(let [mu (:period wave-net-output) std 30 _ %] [mu std])
                     identity
                     nil)
        shift (adaptive-sample (uniform-discrete 0 period)
                     normal
                     #(let [mu (:shift wave-net-output) std 100 _ %] [mu std])
                     identity
                     nil)]
    {:n num-letters, :a amplitude, :p period, :s shift})))

(defn round-range [upper lower n]
  (min (max lower n) upper))

(defn subregion [captcha a b c d] (.subregion captcha a b c d))

(with-primitive-procedures [subregion forward]
(defm sample-x-pos [baseline-captcha x-pos y-pos]
  (let [weights (vec (forward position-net (subregion baseline-captcha 20 20 15 15)))]
  (adaptive-sample (uniform-discrete 0 WIDTH)
                   normal
                   #(let [_ %
                          mu (first weights)
                          std 2]
                      [mu std])
                   identity
                   nil))))

(with-primitive-procedures [eye join generateBaseline makeCaptcha split dot hypot get-coords equalizeHist ripple
                            drawLetter globalBlur getPixels reduce-dim scalar-multiply clear shrink blurBaseline
                            nextInt drawLine drawText dilate predict-num-letters forward]
(defquery guess-captcha [baseline-captcha]
  (let [p (get-net-output baseline-captcha)
        num-letters (:n p)
        amplitude (:a p)
        period (:p p)
        shift (:s p)

        ;; note x-pos is not sampled here!!!
        y-pos (sample (uniform-discrete 0 LENGTH))

        ;; not latent variables, but include to change if necessary
        is-present true
        color 255
        theta 0

        rendered-sigma (sample (uniform-continuous 0.4 2))
        baseline-sigma (sample (uniform-continuous 1 2))

        letter-params (do (clear rendered-captcha)
                    (loop [i 0
                           x-pos []
                           letters []
                           thickness []
                           letter-sigma []
                           scale []]
                      (if (= i num-letters) {:x-pos x-pos, :letters letters, :thickness thickness, :letter-sigma letter-sigma, :scale scale}
                        (let [_x-pos (sample-x-pos baseline-captcha x-pos y-pos)
                              _index (adaptive-sample (uniform-discrete 0 (count alphabet))
                                         discrete
                                         #(let [weights (do (.forward letter-net %)
                                                            (.normalize num-net))] [(vec weights)])
                                         identity
                                         baseline-captcha)
                              _letter (str (nth alphabet _index))
                              _thickness (sample (uniform-discrete 2 6))
                              _letter-sigma (sample (uniform-continuous 0 2))
                              _scale (sample (uniform-discrete 2 4))]

                          (drawLetter rendered-captcha _letter _x-pos y-pos _scale theta _thickness
                                      _letter-sigma is-present color)
                          (recur (inc i) (conj x-pos _x-pos) (conj letters _letter) (conj thickness _thickness)
                                 (conj letter-sigma _letter-sigma) (conj scale _scale))))))
        x-pos (:x-pos letter-params)
        letters (:letters letter-params)
        thickness (:thickness letter-params)
        letter-sigmas (:letter-sigma letter-params)
        scale (:scale letter-params)]

    ;; perform global operations
    (globalBlur rendered-captcha rendered-sigma)
    (blurBaseline baseline-captcha baseline-sigma)
    (ripple rendered-captcha amplitude period shift)

    (shrink rendered-captcha)
    (shrink baseline-captcha)

    (let [rendered-pixels (reduce-dim (vec (getPixels rendered-captcha)))
          baseline-pixels (reduce-dim (vec (getPixels baseline-captcha)))
          std (* 100 (sample (uniform-continuous 9 20))) ; 100 7 25
          ]
      (doall (map #(observe (normal %1 std) %2) rendered-pixels baseline-pixels)) ;2500 ; 4066
;;       (observe (flip 1) (> (min (apply min x-pos) (apply min y-pos)) 10))

      (predict :text letters)
      (predict :x x-pos)
      (predict :y y-pos)
      (predict :scale scale)
      (predict :theta theta)
      (predict :thickness thickness)
      (predict :sigma letter-sigmas)
      (predict :amplitude amplitude)
      (predict :period period)
      (predict :shift shift)
      (predict :color color)
      (predict :is-present is-present)
))))


;; (def baseline (Captcha/generateBaseline 1))
(def baseline (Captcha. LENGTH WIDTH))
(drawText baseline "wave" 10 40 3 2 0.8 0)
(ripple baseline 10 200 10)

(predict-num-letters baseline)


;; (sample (discrete #((let [] [0.5 0.5]))))
;; (.saveImg baseline)

(.denoise baseline)

(.dilate baseline)
(globalBlur baseline 1)
;; (.showImg baseline)



(def stime (System/currentTimeMillis))
;; (def sampler (doquery :rmh guess-captcha [baseline ripple-proposal] :alpha 1 :sigma 4)) ; 0.8 3.3

(def sampler (doquery :smc guess-captcha [baseline] :number-of-particles 2))
(get-predicts (nth sampler 1))



(render-predicts (get-predicts (nth sampler 1)))

;; (def sample-rate 1)
;; (def max-runs 100)
;; (loop [num-tries 1] ; can also append likelihoods
;;   (if (> num-tries  (* 1 max-runs))
;;     1
;;     (let [c (get-predicts (first (drop num-tries sampler)))
;;           captcha (Captcha. LENGTH WIDTH)]
;; ;;          (drawText captcha (:text c) (:x c) (:y c) (:scale c) (:thickness c) (:spacing c) (:sigma c))
;;             (doall (map #(drawLetter captcha %1 %2 %3 %4 %5 %6 %7 %8 %9)
;;                   (:text c) (:x c) (:y c) (:scale c) (:theta c) (:thickness c) (:sigma c) (:is-present c) (:color c)))
;;         (ripple captcha (:amplitude c) (:period c) (:shift c))
;;       (.saveImg captcha)

;;     (recur (+ num-tries sample-rate)))))

;; (.beep (Toolkit/getDefaultToolkit))
;; (float (/ (- (System/currentTimeMillis) stime) 60000))


(+ 1 1)

;; (def likelihoods (map get-log-weight (take max-runs sampler)))
;; (save-weights "weights.txt" likelihoods)



;; (def predicts (map get-predicts (take max-runs sampler)))
;; (apply min-key :error predicts)

