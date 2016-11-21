(ns dm5.core
    (:require [datomic.api :as d]))


;; === DB Setup ===

(let [db-uri "datomic:mem://deepamehta"]
  (d/create-database db-uri)
  (def conn (d/connect db-uri)))

;; install schema
@(d/transact conn (load-file "../dm5-core/resources/dm5-core-schema.edn"))


;; === DB Access ===

(def rule-set '[
  ;; assoc type filter: ?t is type of a?
  [(assoc-type ?a ?t)
   [?a :dm5.object/type ?t]]
  ;; 1-hop traversal: ?rel-t is a related topic of ?t
  [(related-topic ?t ?rel-t)
   [?r1 :dm5.role/player ?t]
   [?a :dm5.assoc/role ?r1]
   [?a :dm5.assoc/role ?r2]
   [(!= ?r1 ?r2)]
   [?r2 :dm5.role/player ?rel-t]]
])

(defn fetch-topic [id] (d/touch (d/entity (d/db conn) id)))

(defn fetch-related-topics [id] (d/q '[:find ?t :in $ % ?id :where (related-topic ?id ?t)] (d/db conn) rule-set id))

(defn all-topics [] (d/q '[:find (pull ?p [*]) :where [?p :dm5/entity-type :dm5.entity-type/topic]] (d/db conn)))
(defn all-assocs [] (d/q '[:find (pull ?p [*]) :where [?p :dm5/entity-type :dm5.entity-type/assoc]] (d/db conn)))

(defn store-topic [uri type value]
  (let [tempid (d/tempid :db.part/user)
        tx-info @(d/transact conn [{:db/id tempid
                                    :dm5/entity-type :dm5.entity-type/topic
                                    :dm5.object/uri uri
                                    :dm5.object/type type
                                    :dm5.object/string-value value}])]
       (d/resolve-tempid (d/db conn) (:tempids tx-info) tempid)))

(defn store-assoc [uri type value roles]
  (let [tempid (d/tempid :db.part/user)
        tx-info @(d/transact conn [{:db/id tempid
                                    :dm5/entity-type :dm5.entity-type/assoc
                                    :dm5.object/uri uri
                                    :dm5.object/type type
                                    :dm5.object/string-value value
                                    :dm5.assoc/role roles}])]
       (d/resolve-tempid (d/db conn) (:tempids tx-info ) tempid)))


;; === Storage API ===

(defprotocol DeepaMehtaStorage
  (fetchTopic [this id])
  (storeTopic [this uri type value])
)

(defrecord DatomicStorage []
  DeepaMehtaStorage
    (fetchTopic [this id]             (fetch-topic id))
    (storeTopic [this uri type value] (store-topic uri type value))
)
