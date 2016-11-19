(ns dm5.core
    (:require [datomic.api :as d]))


;; === Core API ===

(defrecord TopicModelImpl [id uri type-uri value childs]
  de.deepamehta.core.model.TopicModel
    (getId [_] id)
    (getUri [_] uri)
    (getTypeUri [_] type-uri)
    (getSimpleValue [_] value)
    (getChildTopicsModel [_] childs))


;; === Setup DB ===

(def db-uri "datomic:mem://deepamehta")

(d/create-database db-uri)

(def conn (d/connect db-uri))

@(d/transact conn (read-string (slurp "resources/dm5-core-schema.edn")))


;; === Storage API ===

(defn get-topic [id] (let [entity (d/entity (d/db conn) id)] (TopicModelImpl.
  (:db/id entity)
  (:dm5.object/uri entity)
  (:dm5.object/type-uri entity)
  (:dm5.object/string-value entity)
  nil)))

(defn all-topics [] (d/q '[:find (pull ?p [*]) :where [?p :dm5.object/type-uri]] (d/db conn)))

(defn create-topic [uri type-uri value]
  @(d/transact conn [{
  :db/id #db/id[:db.part/user -1]
  :dm5.object/uri uri
  :dm5.object/type-uri type-uri
  :dm5.object/string-value value
  }]))
