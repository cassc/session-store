# session-store

A Clojure library which provides a ring SessionStore implementation backed by redis.

## Usage

Add the following dependency to `project.clj`:

```
[org.clojars.august/session-store "0.1.0"]
```

Configure redis session before starting your ring server:

```clojure
;; configure redis connection
(config-rds-session! {:server {:pool {}
                               :spec {:host     "127.0.0.1" ;; redis ip
                                      :port     6379 ;; redis port
                                      :db       1 ;; redis db number optional
                                      :password "YOUR-REDIS-PASSWORD" ;; redis password
                                      }}
                      :session-ttl 3600 ;; session timeout in seconds
                      :session-prefix "rds-session-prefix" ;; a prefix for session key
                      })

;; If you are using vanilla ring:
(wrap-session ring-handler {:store (redis-session-store)})


;; If you are using lib-noir:
(noir.util.middleware/app-handler
 [your-routes]
 :ring-defaults (assoc-in site-defaults [:session :store] (redis-session-store)))
```

## License

Copyright © 2016 Chen Li

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
