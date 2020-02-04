(defproject net.snca/cachify "0.0.1"
  :description "Make your functions cached"
  :url "https://github.com/anekos/cachify"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clojure.java-time "0.3.2"]
                 [com.taoensso/nippy "2.14.0"]
                 [me.raynes/fs "1.4.6"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :profiles {:dev
             {:dependencies [[org.clojure/clojure "1.10.1"]
                             [org.clojure/tools.namespace "0.3.1"]]
              :source-paths ["src" "dev"]
              :repl-options {:init-ns user}}}
  :deploy-repositories [["releases" :clojars]]
  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[net\\.snca\\\\/cachify \"[0-9.]*\"\\\\]/[net\\.snca\\\\/cachify \"${:version}\"]/" "README.md"]}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
