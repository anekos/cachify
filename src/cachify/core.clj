(ns cachify.core
  (:gen-class)
  (:import [java.io DataInputStream DataOutputStream])
  (:require
    [clojure.java.io :refer [file input-stream output-stream]]
    [java-time :as tm]
    [me.raynes.fs :as fs]
    [taoensso.nippy :as nippy]))


(def ^:dynamic *cache-dir* (file (fs/home) ".local/var/cache/cachify"))
(def ^:dynamic *cache-list* (atom (list)))


(declare is-fresh? now)


(defn- cache-file [cache-name]
  ((condp #(%1 %2)  cache-name
     keyword? #(file *cache-dir* (str (name %) ".edn"))
     string? file
     constantly)
   cache-name))

(defn- call [cache func args]
  (let [value (apply func args)]
    (swap!
     cache
     assoc
     args
     [(now)
      value])
    value))

(defn- clean-up [entries ttl]
  (into {}
        (filter
         (comp #(is-fresh? % ttl) first val)
         entries)))

(defn- is-fresh? [updated-at ttl]
  (if ttl
    (tm/before? (now)
                (tm/plus updated-at ttl))
    true))

(defn- load-cache [cache-name]
  (let [f (cache-file cache-name)]
    (if (.exists f)
      (with-open [r (input-stream f)]
        (nippy/thaw-from-in! (DataInputStream. r)))
      {})))

(defn- load-or-call [cache func args ttl]
  (let [[updated-at value] (@cache args)]
    (if (and updated-at (is-fresh? updated-at ttl))
      value
      (call cache func args))))

(defn- make-cache [cache-name ttl]
  (let [entries (atom (load-cache cache-name))]
    (swap!  *cache-list* conj {:entries entries
                               :ttl ttl
                               :name cache-name})
    entries))

(defn- now []
  (tm/offset-date-time (tm/zone-offset 0)))


(defn cachify
  "Returns the function with a cache

  e.g.
  (defn plus [a b]
    (println a \"+\" b)
    (+ a b))
  (def cached-plus (cachify :plus plus))
  (cached-plus 1 2) ; print \"1 + 2\" and return 3
  (cached-plus 1 2) ; return 3"
  [cache-name func & {:keys [ttl] :or {ttl (tm/seconds 60)}}]
  (let [cache (make-cache cache-name ttl)]
    (fn [& args]
      (load-or-call cache func args ttl))))

(defn perm-1
  "Write a cache to a file"
  [cache]
  (let [{:keys [entries ttl name]} cache
        f (cache-file name)]
    (fs/mkdirs (.getParent f))
    (with-open [w (output-stream f)]
      (nippy/freeze-to-out! (DataOutputStream. w)
                            (clean-up @entries ttl)))))

(defn perm
  "Write caches to files

  e.g.
  (defcachify plus [a b] (+ a b))
  (defn -main []
    (println (plus 1 2))
    (println (plus 1 2))
    ; ...
    (perm)"
  ([]
   (perm @*cache-list*))
  ([caches]
   (doseq [cache caches]
     (perm-1 cache))))

(defmacro defcachify
  "Define cachified function

  e.g.
  (defcachify plus [a b]
    (println a \"+\" b)
    (+ a b))
  (cached-plus 1 2) ; print \"1 + 2\" and return 3
  (cached-plus 1 2) ; return 3"
  [name & body]
  `(def ~name
     (cachify
       ~(keyword name)
       (fn ~@body))))