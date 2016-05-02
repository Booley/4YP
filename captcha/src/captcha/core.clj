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


;; (def net (CNN. "/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/models/captchanet_deploy.prototxt" "/Users/bomoon/Documents/Eclipse Workspace/OpenCVTest/models/snapshots/captchanet_iter_100.caffemodel"))


(def wave-net (CNN. "/Users/bomoon/Documents/caffe-old/bo_models/wave_net/wave.prototxt"
                    "/Users/bomoon/Documents/caffe-old/bo_models/wave_net/wave_weights.caffemodel"
                    64 "ip3") )
(def letter-net (CNN. "/Users/bomoon/Documents/caffe-old/bo_models/letter_net/letter.prototxt"
                    "/Users/bomoon/Documents/caffe-old/bo_models/letter_net/letter_weights.caffemodel"
                      28 "prob") )
(def position-net (CNN. "/Users/bomoon/Documents/caffe-old/bo_models/position_net/position.prototxt"
                    "/Users/bomoon/Documents/caffe-old/bo_models/position_net/snapshots/position_iter_10000.caffemodel"
                        28 "ip3") )
;; (def num-net (CNN. "/Users/bomoon/Documents/caffe-old/bo_models/num_net/num.prototxt"
;;                     "/Users/bomoon/Documents/caffe-old/bo_models/num_net/snapshots/num_iter_10000.caffemodel") )
(def forward #(.forward %1 %2))
(def normalize #(.normalize %))

(def alphabet "ABCDEFGHIJKLMNOPQRSTUVWXXYZabcdefghijklmnopqrstuvwxyz")
(def new-dim-size 300) ; 700 300 LAST THING CHANGED
(def SHRINK_LENGTH 70)
(def SHRINK_WIDTH 150)

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
(def ripple #(.rippleCentered %1 %2 %3 %4))
(def dilate #(.dilate %))
(def resetBaseline #(.resetBaseline %))

(defn render-predicts [c]
  (let [captcha (Captcha. (getWidth baseline) (getHeight baseline))]
    (doall (map #(drawLetter captcha %1 %2 (:y c) %3 0 %4 %5 true 255)
                  (:letters c) (:x c) (:scale c) (:thickness c) (:sigma c)))
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


(defn save-weights [filename w]
  (do
    (spit filename "")
    (spit filename (join "\n" w) :append true) w))



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
;; (defn predict-num-letters [captcha]
;;   (let [weights (do (.forward num-net captcha)
;;                     (.normalize num-net))]
;;     (vec weights)
;; ))

(with-primitive-procedures [forward]
(defm get-net-output [c]
  (let [
         num-letters (sample (uniform-discrete 3 10))
;;          num-letters (+ 3 (adaptive-sample (uniform-discrete 0 7)
;;                            discrete
;;                            #(let [weights (.forward num-net %)] [(vec weights)])
;;                            identity
;;                            c))
        wave-net-output (let [weights (vec (forward wave-net c))]
                          {:amplitude (max 0 (first weights)),
                           :period (max 0 (second weights)),
                           :shift (max 0 (nth weights 2))})
        amplitude (adaptive-sample (uniform-continuous 0 12)
                     normal
                     #(let [mu (:amplitude wave-net-output) std 20 _ %] [mu std])
                     identity
                     nil)
        period (adaptive-sample (uniform-continuous 190 500)
                     normal
                     #(let [mu (:period wave-net-output) std 30 _ %] [mu std])
                     identity
                     nil)
        shift (adaptive-sample (uniform-continuous 0 period)
                     normal
                     #(let [mu (:shift wave-net-output) std 100 _ %] [mu std])
                     identity
                     nil)]
    {:n num-letters, :a amplitude, :p period, :s shift})))

(defn round-range [upper lower n]
  (min (max lower n) upper))

(defn subregion [captcha a b c d] (.subregion captcha a b c d))


(with-primitive-procedures [eye join generateBaseline makeCaptcha split dot hypot get-coords equalizeHist ripple
                            drawLetter globalBlur getPixels reduce-dim scalar-multiply clear shrink blurBaseline
                            nextInt drawLine drawText dilate forward round-range resetBaseline]
(defquery guess-captcha [baseline-captcha]
  (let [p (get-net-output baseline-captcha)
        num-letters (:n p)
        amplitude (:a p)
        period (:p p)
        shift (:s p)

        ;; note x-pos is not sampled here!!!
        ;; all letters have the same y-pos!
        y-pos (adaptive-sample (uniform-discrete 0 LENGTH)
                   normal
                   #(let [weights (vec (forward position-net %))
                          mu (second weights)
                          std 5]
                      [mu std])
                   identity
                   baseline-captcha)

        ;; not latent variables, but include to change if necessary
        is-present true
        color 255
        theta 0

        rendered-sigma (sample (uniform-continuous 0.4 2))
        baseline-sigma (sample (uniform-continuous 1 2))

        letter-params (do (clear rendered-captcha) (resetBaseline baseline-captcha)
                    (loop [i 0
                           x-pos []
                           letters []
                           thickness []
                           letter-sigma []
                           scale []]
                      (if (= i num-letters) {:x-pos x-pos, :letters letters, :thickness thickness, :letter-sigma letter-sigma, :scale scale}
                        (let [_ (resetBaseline baseline-captcha)
                              _x-pos (sample-x-pos baseline-captcha x-pos)
                              _index (sample-index baseline-captcha _x-pos y-pos)
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



;; (predict-num-letters baseline)


;; (sample (discrete #((let [] [0.5 0.5]))))
;; (.saveImg baseline)

;; (.denoise baseline)

;; (.dilate baseline)
;; (globalBlur baseline 1)
(.storeBaseline baseline)
(.showImg baseline)
;; (resetBaseline baseline)

;; (vec (.forward letter-net baseline 28))



(def stime (System/currentTimeMillis))
;; (def sampler (doquery :rmh guess-captcha [baseline ripple-proposal] :alpha 1 :sigma 4)) ; 0.8 3.3

(def sampler (doquery :lmh guess-captcha [baseline]))

(def num-particles 1)
;; (def sampler (doquery :smc guess-captcha [baseline] :number-of-particles num-particles))
(nth sampler 3)
(get-predicts (nth sampler 1))

(render-predicts (get-predicts (nth sampler (dec num-particles))))

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


(def baseline (Captcha. 150 70))
;; (drawText baseline "wave" 10 40 3 2 0.8 0)
;; (ripple baseline 10 200 10)
;; (.showImg baseline)
(drawLetter baseline "A" 30 40 2 0 3 1 true 255)
(drawLetter baseline "B" 60 40 2 0 3 1 true 255)
(drawLetter baseline "C" 90 40 2 0 3 1 true 255)
(drawLetter baseline "D" 120 40 2 0 3 1 true 255)
;; (drawLetter baseline "E" 110 40 2 0 3 1 true 255)
(.addNoise baseline 800)
(ripple baseline 12 178 112)
(.showImg baseline)

(vec (forward wave-net baseline))
(rescale (forward position-net baseline))
(defn rescale [p]
  (let [x (first (vec p))
        y (second (vec p))]
    (vector (* x (/ 150 240)) (* y (/ 70 110)))))

(ripple baseline 8.43 174.69 66)



;; Captcha method wrappers
(def getWidth #(.width %))
(def getHeight #(.height %))
(def clone #(.clone %))
(def crop #(.crop %1 %2))
(def add-vecs #(op/+ %1 (if (nil? %2) 0 %2)))

(defn makeRandom [] (Random.))
(def rdm (makeRandom))
(defn nextInt [rdm bound] (.nextInt rdm bound))

(with-primitive-procedures [getWidth getHeight clone globalBlur forward crop add-vecs makeCaptcha
                            drawLetter getPixels nextInt reduce-dim]
(defm run-wave-nn [c]
  (let [wave-net-output (let [weights (vec (forward wave-net c))]
                          {:amplitude (max 0 (first weights)),
                           :period (max 0 (second weights)),
                           :shift (max 0 (nth weights 2))})
        amplitude (adaptive-sample (uniform-continuous 0 12)
                     bounded-normal
                     #(let [mu (:amplitude %) std 4] [mu std 1 11])
                     identity wave-net-output)
        period (adaptive-sample (uniform-continuous 190 500)
                     bounded-normal
                     #(let [mu (:period %) std 100 _ %] [mu std 191 499])
                     identity wave-net-output)
        shift (adaptive-sample (uniform-continuous 0 period)
                     bounded-normal
                     #(let [mu (:shift %) std 100 _ %] [mu std 1 (dec period)])
                     identity wave-net-output)]
    {:a amplitude, :p period, :s shift}
))

(defm get-init-pos [c WIDTH HEIGHT]
  (let [position-net-output (vec (forward position-net c))]
    {:x (adaptive-sample (uniform-continuous 15 (- WIDTH 15))
                         bounded-normal
                         #(let [mu (* (first %) (/ WIDTH 240)) std 5] [mu std 15 (- WIDTH 15)])
                         identity position-net-output),
     :y (adaptive-sample (uniform-continuous 15 (- HEIGHT 15))
                         bounded-normal
                         #(let [mu (* (second %) (/ HEIGHT 110)) std 5] [mu std 15 (- WIDTH 15)])
                         identity position-net-output)}
))

;; TODO: use adaptive sample
(defm find-letter-pos [c positions WIDTH HEIGHT]
  (let [letter-width 35
        crop-x (if (zero? (count positions)) 0 (min (- WIDTH 2) (+ letter-width (first (last positions)))))
        submat (crop c crop-x)
        pos-net-output (vec (forward position-net submat))
       ]
;;     [(adaptive-sample (uniform-continuous 15 (- WIDTH 15))
;;                          normal
;;                          #(let [mu (* (first %) (/ WIDTH 240)) std 150] [mu std])
;;                          identity pos-net-output),
;;      (adaptive-sample (uniform-continuous 15 (- HEIGHT 15))
;;                          normal
;;                          #(let [mu (* (second %) (/ HEIGHT 110)) std 40] [mu std])
;;                          identity pos-net-output)]
    pos-net-output
))

(defm get-letter-vars [num-letters WIDTH HEIGHT]
  (loop [i 0
        positions []
        letters []]
   (if (= i num-letters) {:positions positions, :letters letters}
     (let [pos-net-output (find-letter-pos baseline positions WIDTH HEIGHT)
           x-pos (max 15 (adaptive-sample (uniform-continuous 15 (- WIDTH 15))
                       bounded-normal
                       #(let [mu (* (first %) (/ WIDTH 240)) std 5] [mu std 16 (- WIDTH 16)])
                       identity pos-net-output))
           y-pos (max 15 (adaptive-sample (uniform-continuous 15 (- HEIGHT 15))
                       bounded-normal
                       #(let [mu (* (second %) (/ HEIGHT 110)) std 5] [mu std 16 (- HEIGHT 16)])
                       identity pos-net-output))

           letter-index (sample (uniform-discrete 0 (count alphabet)))
           letter (str (nth alphabet letter-index))]
       (recur (inc i) (conj positions (add-vecs [x-pos y-pos]
                                                [(if (zero? (count positions)) 0 (first (last positions))) 0]))
              (conj letters letter))))))

(defquery read-captcha [original-baseline-captcha]
  (let [;; define reference vars
        baseline (clone original-baseline-captcha)
        WIDTH (getWidth original-baseline-captcha)
        HEIGHT (getHeight original-baseline-captcha)
        rendered (makeCaptcha WIDTH HEIGHT)

        ;; begin sampling vars
        num-letters (sample (uniform-discrete 3 9))

;;         ;; run wave net
        wave-output (run-wave-nn baseline)
        amplitude (:a wave-output)
        period (:p wave-output)
        shift (:s wave-output)

        ;; run position net
        init-position (get-init-pos baseline WIDTH HEIGHT)
        init-x (:x init-position)
        init-y (:y init-position)

        ;; sample other vars, either not latent (yet) or don't have nets
        rendered-sigma (sample (uniform-continuous 0.4 2))
        baseline-sigma (sample (uniform-continuous 1 2))
        thickness (repeatedly num-letters #(sample (uniform-discrete 2 6)))
        letter-sigma (repeatedly num-letters #(sample (uniform-continuous 0 2)))
        scale (repeatedly num-letters #(sample (uniform-discrete 2 4)))

        is-present true
        color 255
        theta 0

        ;; use nets to predict letter position and identity, iteratively
        letter-vars (get-letter-vars num-letters WIDTH HEIGHT )
         ]

    ;; render image
    (map #(drawLetter rendered %1 %2 init-y %3 theta %4 %5 is-present color)
         (:letters letter-vars) (map first (:positions letter-vars))
         scale thickness letter-sigma)
    (globalBlur rendered rendered-sigma)

    (let [rendered-pixels (reduce-dim (vec (getPixels rendered)))
          baseline-pixels (reduce-dim (vec (getPixels baseline)))]
;;           num-random-pixels (/ (* WIDTH HEIGHT) 2)
;;           indices (repeatedly num-random-pixels #(nextInt rdm (count rendered-pixels)))]
;;       (doall (map #(observe (flip 1) (<= (second %) HEIGHT)) (:positions letter-vars)))
;;       (doall (map #(observe (flip 1) (<= (first %) WIDTH)) (:positions letter-vars)))
      (doall (map #(observe (normal %1 5000) %2) rendered-pixels baseline-pixels))
      )

    (predict :num-letters num-letters)
    (predict :amplitude amplitude)
    (predict :period period)
    (predict :shift shift)
    (predict :x (map first (:positions letter-vars)))
;;     (predict :y (map second (:positions letter-vars)))
    (predict :y init-y)
    (predict :sigma letter-sigma)
    (predict :thickness thickness)
    (predict :letters (:letters letter-vars))
    (predict :scale scale)
    (predict :blur rendered-sigma)
)))

(def num-particles 50)
(def sampler (doquery :smc read-captcha [baseline] :number-of-particles num-particles))
(with-out-str (time (nth sampler (dec num-particles))))
(nth sampler (dec num-particles))
(.showImg baseline)

(def posterior ((conditional read-captcha :smc :number-of-particles num-particles) baseline))
(def num-samples num-particles)
(def result (repeatedly num-samples #(sample posterior)))
(last result)

;; (render-predicts (last result))
(vec (forward position-net baseline))
(* 26 (/ 150 240.0))
(* 73 (/ 70 110.0))

(let [c (last result)
       captcha (Captcha. (getWidth baseline) (getHeight baseline))]
    (doall (map #(drawLetter captcha %1 %2 (:y c) %3 0 %4 %5 true 255)
                  (:letters c) (:x c) (:scale c) (:thickness c) (:sigma c)))
    (ripple captcha (:amplitude c) (:period c) (:shift c))
  (globalBlur captcha (:blur c))
  (.showImg captcha))

;; (render-predicts (get-predicts (nth sampler (dec num-particles))))
(.showImg baseline)

(render-predicts (get-predicts (nth sampler 6)))
(nth sampler 18)



(defn markPoints [c pos]
  (map #(.drawCircle c (first %) (second %) 5 255 3) pos))
;; (markPoints baseline [[32.63314874220046 68.22892198271965] [76.71837154154466 87.12912146403134] [101.56839051170493 95.06499449847719] [124.31746088037221 80.53787318423007] [182.72423711742124 83.34101120544398] [224.03264394033403 77.16722766547159]])
;; (.showImg baseline)

;; (with-primitive-procedures [forward]
;;   (defm do-nothing []
;;     (let [z (adaptive-sample (uniform-continuous 0 10)
;;                            normal
;;                            #(let [_ %] [10 5])
;;                            identity
;;                            nil)
;;         a (adaptive-sample (uniform-continuous 0 10)
;;                            normal
;;                            #(let [_ %] [10 5])
;;                            identity
;;                            nil)
;;         b (sample (uniform-discrete 0 10))]
;;       a))

;; (defquery simple []
;;   (let [
;;          x (adaptive-sample (uniform-continuous 0 10)
;;                            normal
;;                            #(let [_ %] [-10 300])
;;                            identity
;;                            nil)
;;         y (adaptive-sample (uniform-continuous 0 12)
;;                      normal
;;                      #(let [_ % mu 8 std 5] [mu std])
;;                      identity nil)
;; ;;         z (adaptive-sample (uniform-continuous 0 10)
;; ;;                            normal
;; ;;                            #(let [_ %] [10 5])
;; ;;                            identity
;; ;;                            nil)
;; ;;         a (adaptive-sample (uniform-continuous 0 10)
;; ;;                            normal
;; ;;                            #(let [_ %] [10 5])
;; ;;                            identity
;; ;;                            nil)
;;         b (sample (uniform-discrete 0 10))]
;;     (observe (normal 3 1) 10)
;;     (predict :x b))))

;; (vec (forward wave-net baseline))

;; (def record (loop [i 0 results []]
;;   (if (= i 10) results
;;     (let [test-sampler (doquery :smc simple [] :number-of-particles 500)]
;;   (recur (inc i) (conj results (nth test-sampler 500)))))))
;; (map :log-weight record)





