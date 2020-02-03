# cachify
[![Build Status](https://travis-ci.org/anekos/cachify.svg?branch=master)](https://travis-ci.org/anekos/cachify)
[![codecov](https://codecov.io/gh/anekos/cachify/branch/master/graph/badge.svg)](https://codecov.io/gh/anekos/cachify)
[![Clojars Project](https://img.shields.io/clojars/v/net.snca/cachify.svg)](https://clojars.org/net.snca/cachify)


Make your functions cached.


```clj
[net.snca/cachify "0.0.1"]
```

## Usage

```
(require '[cachify.core :refer :all])

(def plus (cachify :plus (fn [a b]
                           (println a "+" b)
                           (+ a b))))

(defcachify minus
  [a b]
  (println a "-" b)
  (- a b))


(defn -main []
  (println (plus 1 2)) ; → 1 + 2
                       ;    3
  (println (plus 1 2)) ; → 3

  (println (minus 1 2)) ; → 1 - 2
                        ;    -1
  (println (minus 1 2)) ; → -1

  (perm) ; Write all caches to files
  )
```

## License

Copyright © 2020 anekos

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
