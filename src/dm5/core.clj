(ns dm5.core
  (:require [dm5.datomic :as db]))

(declare unify)

(defn create-topic [type value]
  (unify type value))

(declare unify-simple)
(declare unify-composite)
(declare simple?)

(defn unify
  "Returns the ID of the unified object"
  [type value]
  (if (simple? type) (unify-simple type value) (unify-composite type value))
)

(declare parent-lookup)
(declare with-assoc-defs)

(defn unify-simple [type value]
  (let [id (db/fetch-by-value (name type) value)]
       (if id id (db/store-topic "" (name type) value))))

(defn unify-composite [type value]
  (parent-lookup type (with-assoc-defs type (fn [ct] {:type ct :id (unify ct (ct value))}))))

(defn parent-lookup [parent-type child-ids]
  (println "parent-lookup" parent-type child-ids))

(declare type-defs)

(defn type-def
  "Looks up a type definition"
  [type] (let [type-def (type type-defs)]
              (assert type-def (str "Unknown type definition " type))
              type-def))

(defn with-assoc-defs [type f]
   (map #(f (key %)) (-> type type-def :assoc-defs)))

(defn composite? [type] (= (-> type type-def :data-type) :composite))

(defn simple? [type] (not (composite? type)))


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
