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
                 [opencv/opencv-native "2.4.12"]]
  :main ^:skip-aot captcha.core
  :target-path "target/%s"
  :java-source-paths ["../CaptchaTools/src/"]
  :resource-paths ["/Users/bomoon/Documents/JavaCPP/javacpp-presets/BACKUP_JARS/target/opencv.jar"
                   "/Users/bomoon/Documents/JavaCPP/javacpp-presets/BACKUP_JARS/target/caffe.jar"
                   "/Users/bomoon/Documents/JavaCPP/javacpp-presets/BACKUP_JARS/target/caffe-macosx-x86_64.jar"
                   "/Users/bomoon/Documents/JavaCPP/javacpp-presets/BACKUP_JARS/target/opencv-macosx-x86_64.jar"]
  :profiles {:uberjar {:aot :all}})
