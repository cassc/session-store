# session-store

A Clojure library which provides a ring SessionStore implementation backed by redis.

## Usage

```clojure
(config-rds-session! {:server rds-server
                      :session-ttl session-ttl-in-secs
                      :session-prefix session-key-prefix})

;; with noir middleware
(noir.util.middleware/app-handler
 [your-routes]
 :ring-defaults (assoc-in site-defaults [:session :store] (redis-session-store)))
```

## License

Copyright © 2016 Chen Li

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
