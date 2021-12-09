# NSA
Namespace Agency - Track your function calls/responses in a global state.

Heavily inspired by `tortue/spy`.

## Install
Do a local install (necessary because the package is not in a public artifact
thing like Maven Central or Clojars):

```
./00_local-install.sh
```

Then merge this into your ~/.lein/profiles.clj file:

```
{:user {:dependencies [;; nsa creates a global state to track funtion calls and responses.
                       ;; NOTE: It's not on clojars/maven, so run the following:   `git clone --depth=1 https://git.sr.ht/~harryvederci/nsa && cd nsa && ./00_local-install.sh`
                       [harryvederci/nsa "0.0.1"]] ;; TODO: check if this is the latest version.
        :injections [(require 'nsa.spy)]}}
```

If you use something else than Leiningen, you can probably do something similar
to its configuration to make that work.


## Usage
You can now create a spy. What that means is that incoming (= calls) and
outgoing (= responses) data to/from that function will be stored in the
`nsa.spy/spy-db` atom.

### Creating a spy
Say you have the following function:
```
(ns com.megacorp.utils)

(defn my-sum [x y]
  (+ x y))
```

Wherever you like, run the following:
```
(nsa.spy/spy-on #'com.megacorp.utils/my-sum)
```

### Workflow
I usually have something like the following in my `<project root>/dev/user.clj`
file, so I can easily call different spying actions on a function by
(un)commenting lines:

```
(comment
  (-> #'com.megacorp.utils/my-sum
      ; nsa.spy/spy-on
      ; nsa.spy/count-calls
      ; nsa.spy/repeat-latest-call
      nsa.spy/latest-response))
```

The following spying functions are available:
- count-calls
- all-inputs
- first-params
- latest-params
- all-responses
- first-response
- latest-response
- repeat-latest-call

They all take a function (i.e. `#'com.megacorp.utils/my-sum`) as an input.
Their names are pretty descriptive.
