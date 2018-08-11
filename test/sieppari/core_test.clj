(ns sieppari.core-test
  (:require [clojure.test :refer :all]
            [testit.core :refer :all]
            [sieppari.core :as c]))

(defn handler-with-meta
  {:depends #{:foo :bar}}
  [ctx]
  ctx)

(deftest interceptor?-test
  (fact
    (c/interceptor? "foo")
    => false)
  (fact
    (c/interceptor? (c/-interceptor {:name :foo, :handler identity}))
    => true))

(deftest -interceptor-test
  (fact ":name is mandatory"
    (c/-interceptor {})
    => (throws-ex-info "interceptor :name is mandatory"))

  (fact ":name must be a keyword"
    (c/-interceptor {:name "foo"})
    => (throws-ex-info "interceptor :name must be a keyword"))

  (fact "defaults are applied"
    (c/-interceptor {:name :foo
                     :handler str})
    => {:name :foo
        :handler str
        :leave identity
        :error identity
        :applies-to? (fn [f] (true? (f :what-ever)))
        :depends (just #{})})


  (fact "functions can be made to interceptors"
    (c/-interceptor identity)
    => {:name :handler
        :enter identity})

  (let [i (c/-interceptor identity)]
    (fact "interceptors are already interceptors"
      (c/-interceptor i) => i))

  (fact "nil punning"
    (c/-interceptor nil)
    => nil))

(def topology-sort #'c/topology-sort)

(deftest topology-sort-test
  (fact "Topology sort works"
    (topology-sort [{:name :e, :handler identity, :depends #{:b :d}}
                    {:name :d, :handler identity, :depends #{:c}}
                    {:name :a, :handler identity, :depends #{}}
                    {:name :c, :handler identity, :depends #{:a :b}}
                    {:name :b, :handler identity, :depends #{:a}}])
    => [:a :b :c :d :e])
  (fact "Circular dependencies are reported"
    (topology-sort [{:name :a, :handler identity, :depends #{:b}}
                    {:name :b, :handler identity, :depends #{:c}}
                    {:name :c, :handler identity, :depends #{:d}}
                    {:name :d, :handler identity, :depends #{:a}}])
    => (throws-ex-info "interceptors have circular dependency")))

(deftest into-interceptors-test
  ; TODO:
  )
