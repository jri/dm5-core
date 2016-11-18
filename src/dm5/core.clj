(ns dm5.core
    (:require [datomic.api :as d]))


;; === Setup DB ===

(def db-uri "datomic:mem://deepamehta")

(d/create-database db-uri)

(def conn (d/connect db-uri))

@(d/transact conn (read-string (slurp "resources/dm5-core-schema.edn")))


;; === Storage API ===

(defn get-topic [id] (d/touch (d/entity (d/db conn) id)))

(defn all-topics [] (d/q '[:find (pull ?p [*]) :where [?p :dm5.object/type-uri]] (d/db conn)))

(defn create-topic [uri type-uri value]
    @(d/transact conn [{
    :db/id #db/id[:db.part/user -1]
    :dm5.object/uri uri
    :dm5.object/type-uri type-uri
    :dm5.object/string-value value
    }]))
