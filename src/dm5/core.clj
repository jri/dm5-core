(ns dm5.core
  (:require [dm5.datomic :as db]))



;; mockup some type defs
(def types {
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
