(defproject captcha "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [anglican "0.8.0-SNAPSHOT-RMH"]
                 [net.mikera/core.matrix "0.46.0"]
                 [net.mikera/core.matrix.stats "0.5.0"]
                 [net.mikera/vectorz-clj "0.37.0"]
                 [opencv/opencv "2.4.12"]
                 [opencv/opencv-native "2.4.12"]
                 [javacpp/opencv "1.0.0"]
                 [javacpp/opencv-macosx "1.0.0"]
                 [javacpp/caffe "1.0.0"]
                 [javacpp/caffe-macosx "1.0.0"]
                 [javacpp/javacpp "1.0.0"]]
  :main ^:skip-aot captcha.core
  :target-path "target/%s"
  :java-source-paths ["../CaptchaTools/src/"]

  :profiles {:uberjar {:aot :all}})
