(ns dm5.core
    (:require [datomic.api :as d]))


;; === Core API ===

(defrecord TopicModelImpl [id uri type value childs]
  de.deepamehta.core.model.TopicModel
    (getId [_] id)
    (getUri [_] uri)
    (getTypeUri [_] type)
    (getSimpleValue [_] value)
    (getChildTopicsModel [_] childs))


;; === Setup DB ===

(let [db-uri "datomic:mem://deepamehta"]
  (d/create-database db-uri)
  (def conn (d/connect db-uri)))

@(d/transact conn (load-file "resources/dm5-core-schema.edn"))


;; === Storage API ===

(defn get-topic [id] (let [e (d/entity (d/db conn) id)] (TopicModelImpl.
  (:db/id e)
  (:dm5.object/uri e)
  (:dm5.object/type e)
  (:dm5.object/string-value e)
  nil)))

(defn all-topics [] (d/q '[:find (pull ?p [*]) :where [?p :dm5/entity-type :dm5.entity-type/topic]] (d/db conn)))
(defn all-assocs [] (d/q '[:find (pull ?p [*]) :where [?p :dm5/entity-type :dm5.entity-type/assoc]] (d/db conn)))

(defn create-topic [uri type value]
  @(d/transact conn [{:db/id #db/id[:db.part/user]
                      :dm5/entity-type :dm5.entity-type/topic
                      :dm5.object/uri uri
                      :dm5.object/type type
                      :dm5.object/string-value value}]))

(defn create-assoc [uri type value roles]
  @(d/transact conn [{:db/id #db/id[:db.part/user]
                      :dm5/entity-type :dm5.entity-type/assoc
                      :dm5.object/uri uri
                      :dm5.object/type type
                      :dm5.object/string-value value
                      :dm5.assoc/role roles}]))
