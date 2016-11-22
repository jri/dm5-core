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
  ;; 1-hop traversal: ?t is related to ?rel-t via ?a
  [(related-topic ?t ?rel-t ?a)
   [?r1 :dm5.role/player ?t]
   [?a :dm5.assoc/role ?r1]
   [?a :dm5.assoc/role ?r2]
   [(!= ?r1 ?r2)]
   [?r2 :dm5.role/player ?rel-t]]
  ;; type filter: object ?o (topic or assoc) is of type ?t
  [(object-type ?o ?t)
   [?o :dm5.object/type ?t]]
  ;; role type filter: role ?r is of type ?t
  [(role-type ?r ?t)
   [?r :dm5.role/type ?t]]
])

(defn fetch-topic [id] (d/touch (d/entity (d/db conn) id)))

;; traversal

(declare traversal-query)
(declare add-filter)

(defn fetch-related-topics
  ;; TODO: rather pass map instead of varargs in favor for prgrammatic calling
  [id & {:keys [:assoc-type :my-role-type :others-role-type :others-type] :as opts}]
  (let [query (traversal-query id opts)]
       (apply d/q {:find '[?t ?a]
                   :in (query :in)
                   :where (query :where)} (d/db conn) rule-set (query :args))))

(defn traversal-query
  "Returns a traveral query, a map with 3 entries :in :where :args"
  [id opts]
  (let [query {:in '[$ % ?id] :where '[(related-topic ?id ?t ?a)] :args [id]}]
       (-> query (add-filter opts :assoc-type  '?at '(object-type ?a ?at))
                 (add-filter opts :others-type '?ot '(object-type ?t ?ot)))))

(defn add-filter [query opts opt-key in-sym where-sym]
  (let [opt-val (opt-key opts)]
       (if opt-val (-> query (update-in [:in]    conj in-sym)
                             (update-in [:where] conj where-sym)
                             (update-in [:args]  conj opt-val))
                   query)))

;;

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
