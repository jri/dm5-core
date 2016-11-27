(ns dm5.core
  (:require [dm5.datomic :as db])
  (:use [clojure.set :only [intersection]]))

;; mockup some type defs
(def type-defs {
  ;; Person
  :dm5.contacts.person {
    :data-type :composite
    :assoc-defs {
      :dm5.contacts.person_name {:cardinality :one}
      :dm5.contacts.address {:cardinality :one}
    }
  }
  :dm5.contacts.person_name {
    :data-type :composite
    :assoc-defs {
      :dm5.contacts.first_name {:cardinality :one}
      :dm5.contacts.last_name {:cardinality :one}
    }
  }
  :dm5.contacts.first_name {:data-type :text}
  :dm5.contacts.last_name {:data-type :text}
  ;; Address
  :dm5.contacts.address {
    :data-type :composite
    :assoc-defs {
      :dm5.contacts.street {:cardinality :one}
      :dm5.contacts.street_number {:cardinality :one}
      :dm5.contacts.postal_code {:cardinality :one}
      :dm5.contacts.city {:cardinality :one}
      :dm5.contacts.country {:cardinality :one}
    }
  }
  :dm5.contacts.street {:data-type :text}
  :dm5.contacts.street_number {:data-type :text}
  :dm5.contacts.postal_code {:data-type :text}
  :dm5.contacts.city {:data-type :text}
  :dm5.contacts.country {:data-type :text}
})

(defn type-def
  "Looks up a type definition"
  [type]
  (or (type-defs type)
      (assert false (str "Unknown type definition " type))))

(defn with-assoc-defs [type f]
  (map f (-> type type-def :assoc-defs keys)))

(defn composite? [type] (= (-> type type-def :data-type) :composite))

(defn simple? [type] (not (composite? type)))

;;

(declare unify)

(defn nav-to-parents
  "Returns a set of 2-element vectors:
      #{[parent-id assoc-id]}"
  [parent-type child-id]
  (db/fetch-related-topics child-id :assoc-type "dm5.core.hierarchy"
                                    :my-role-type "dm5.core.child"
                                    :others-role-type "dm5.core.parent"
                                    :others-type (name parent-type)))

(defn associate-child-topic [parent-id child-id]
  (println "    associate-child-topic <--" parent-id child-id)
  (db/store-assoc "" "dm5.core.hierarchy" "" [{:dm5.role/player parent-id :dm5.role/type "dm5.core.parent"}
                                              {:dm5.role/player child-id  :dm5.role/type "dm5.core.child"}])
)

(defn id-set
  "rel-objects a set of 2-element vectors:
      #{[rel-obj-id assoc-id]}"
  [rel-objects]
  (set (map first rel-objects)))

(defn parent-lookup
  "child-refs is a vector of 2-entry maps:
      [{:type ... :id ...}]"
  [parent-type child-refs]
  (println "parent-lookup" parent-type child-refs)
  (let [parent-ids (apply intersection (map id-set (map #(nav-to-parents parent-type (:id %)) child-refs)))
        count (count parent-ids)]
    (assert (<= count 1) (str "Ambiguity: there are " count " " parent-type " parents " child-refs))
    (println "    -->" (first parent-ids))
    (first parent-ids)))

(defn create-parent-topic
  "child-refs is a vector of 2-entry maps:
      [{:type ... :id ...}]"
  [parent-type child-refs]
  (println "    create-parent-topic <--" parent-type child-refs)
  (let [parent-id (db/store-topic "" (name parent-type) "")]
    (doseq [{child-id :id} child-refs]
      (associate-child-topic parent-id child-id))
    parent-id))

(defn unify-simple [type value]
  (or (db/fetch-by-value (name type) value)
      (db/store-topic "" (name type) value)))

(defn unify-composite [type value]
  (let [child-refs (with-assoc-defs type (fn [ct] {:type ct :id (unify ct (ct value))}))]
    (or (parent-lookup type child-refs)
        (create-parent-topic type child-refs))))

(defn unify
  "Returns the ID of the unified object"
  [type value]
  (if (simple? type) (unify-simple type value) (unify-composite type value))
)

;;

(defn create-topic [type value]
  (unify type value))
