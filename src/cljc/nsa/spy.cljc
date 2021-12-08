(ns nsa.spy)
(defonce spy-db (atom {}))


(defn- ensure-calls-and-responses-are-vectors
  [k]
  (when-not (-> @spy-db (get k))
    (swap! spy-db
           conj
           [k {:calls (vector)
               :responses (vector)}])))

(defn- record-call!
  [k args]
  (swap! spy-db
         update-in
         [k :calls]
         conj args))

(defn- record-response!
  [k response]
  (swap! spy-db
         update-in
         [k :responses]
         conj response))

(defn- record-exception!
  [k e]
  (swap! spy-db
         update-in
         [k :responses]
         conj {:thrown #?(:clj (Throwable->map e) :cljs e)}))






(defn find-fns
  "
  Find all functions in all namespaces. If that's too much, takes a
  `namespace-prefix` so you can only find the functions of namespaces starting
  with that prefix.

  Example `namespace-prefix` value (notice the quote and the dot):
    'com.megacorp.
  "
  ([]
   (->>
    (all-ns)
    (mapcat ns-publics)
    (vals)))
  ([namespace-prefix]
   (->> (find-fns)
     ; (filter #(:your-meta-keyword (meta %)))
     (filter #(clojure.string/starts-with? (str %) (str "#'" namespace-prefix))))))


(defn fqfn
  "Returns a fully qualified name for the function"
  [function]
  (let [m (meta function)]
    (str (:ns m) "/" (:name m))))


(defn spy-on
  "
  Make the function report its input and output to the NSA (Namespace Agency).

  Input example:
    #'com.megacorp.some-ns/my-function
  "
  [function]
  (alter-var-root
   function
   (fn [f]
     (fn [& f-args]
       (let [function-name (fqfn function)]
         ;; TODO: (ensure-calls-and-responses-are-vectors function-name)
         (record-call! function-name f-args)
         (try
           (let [response (apply f f-args)]
             (record-response! function-name response)
             response)
           (catch Exception e  ;; TODO:    (catch #?(:clj Exception :cljs js/Object) e)
             (record-exception! function-name e)
             (throw e))))))))


(defn spy-on-everything
  "Experimental. Spy on everyone and their mother. Kinda like in real life."
  []
  (map spy-on
       (find-fns)))


(defn spy-on-namespaces-starting-with
  "
  Experimental.
  Spy on all functions in all namespaces starting with `namespace-prefix`.

  Input example:
    'com.megacorp.
  "
  [namespace-prefix]
  (map spy-on
       (find-fns namespace-prefix)))


(defn db-reset
  []
  (reset! spy-db {}))

(defn list-spies []
  (some-> @spy-db keys))

(defn count-calls [function]
  (let [fn-key (fqfn function)]
    (some-> @spy-db (get fn-key) :calls count)))

(defn all-inputs [function]
  (let [fn-key (fqfn function)]
    (some-> @spy-db (get fn-key) :calls)))

(defn first-params [function]
  (-> function all-inputs first))

(defn latest-params [function]
  (-> function all-inputs last))

(defn all-responses [function]
  (let [fn-key (fqfn function)]
    (some-> @spy-db (get fn-key) :responses)))

(defn first-response [function]
  (-> function all-responses first))

(defn latest-response [function]
  (-> function all-responses last))

(defn repeat-latest-call [function]
  (apply function (latest-params function)))

(defn db-export
  "NOTE: this works, but isn't really useful because `db-import` is not
  finished yet.

  Store the `spy-db` atom its content to a file.
  We at the NSA don't discriminate against sensitive data: we store everything.
  In other words: you may want to be careful with where you store it."
  [file-path]
  (spit file-path
        (deref spy-db)
        :append false))

(defn db-import
  "WIP - Overwrites the in-memory nsa db with the one stored in the file at `file-path`."
  [file-path]
  ; (let [readers {'object tagged-literal}])
  ; (let [tag-readers {'object identity}]
  (db-reset)
  (->> file-path
       slurp
       (clojure.edn/read-string
         {; :readers tag-readers
          :default (fn [tag value]
                     value)})
       (swap! spy-db)))



(def get-modified-namespaces
  (ns-tracker.core/ns-tracker [(clojure.java.io/file "src/")
                               ; (clojure.java.io/file "test/")
                               (clojure.java.io/file "frontend/src/cljs/")]))
                               ; (clojure.java.io/file "frontend/src/cljs-test/")]))

#_(doseq [ns-sym (get-modified-namespaces)]
    (println (str "'" ns-sym "' was changed.")))



(comment
  (db-reset)

  (-> #'clojure.string/blank?
      spy-on)

  (clojure.string/blank? "a")
  (clojure.string/blank? -1)

  (list-spies)

  (-> #'clojure.string/blank?
      ; all-inputs
      ; all-responses)
      latest-response)
      ; repeat-latest-call)
      ; first-response
      ; count-calls
      ; latest-response
      ; all-responses)

  (db-export "the-permanent-record.edn")
  (db-import "the-permanent-record.edn")

  (deref spy-db)


  nil)
