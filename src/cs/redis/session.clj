(ns cs.redis.session
  "A redis flavor of SessionStore"
  (:require
   [taoensso.carmine   :as car :refer [wcar]]
   [ring.middleware.session.store :refer [SessionStore]])
  (:import [java.util UUID]))

(def rds-session-config (atom nil))

(defn config-rds-session!
  [rds-configs]
  (reset! rds-session-config rds-configs))

(defmacro wcar* [& body]
  `(car/wcar (:server @rds-session-config) ~@body))

(defn store-code
  "Store a `key`-`val` pair. If `val` is nil, a random string
  will be generated. If `ttl`(secs) is nil, the code will not expire.

  Returns the val stored."
  [{:keys [key val ttl]}]
  {:pre [key val]}
  (wcar*
   (car/multi)
   (car/set key val)
   (when ttl (car/expire key ttl))
   (car/exec))
  val)

(defn get-code
  [{:keys [key]}]
  {:pre [key]}
  (wcar* (car/get key)))

(defn remove-code
  [{:keys [key]}]
  {:pre [key]}
  (wcar* (car/del key)))

(defn touch
  "Touch a key to reset the access time.
  Returns nil if the key is already expired, otherwise returns the value of the key."
  [{:keys [key ttl]}]
  {:pre [key ttl]}
  (first
   (let [key key]
     (wcar*
      (when (car/get key)
        (car/expire key ttl))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Redis session implementation
;; implement a redis session-store type. see source code from ring.middleware.session.memory

(defn- make-session-key [key]
  (str (or (:session-prefix @rds-session-config) (subs (str (UUID/randomUUID)) 25)) key))

(deftype RedisSessionStore []
  SessionStore
  (read-session [_ key]
    (touch {:key (make-session-key key) :ttl (or (:session-ttl @rds-session-config) 7200)}))
  (write-session [_ key data]
    (let [key (or key (str (UUID/randomUUID)))]
      (store-code {:key (make-session-key key) :val data :ttl (or (:session-ttl @rds-session-config) 7200)})
      key))
  (delete-session [_ key]
    (remove-code {:key (make-session-key key)})
    nil))

(defn redis-session-store
  "Redis session store. Redis db and session key prefix are configured
  in edn files."
  []
  (RedisSessionStore.))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

