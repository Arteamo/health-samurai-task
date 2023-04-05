# health-samurai-task

A Clojure CRUD application made as part of a test for job applicants.

## Instruments and practices
* Ring Jetty
* Clojurescript
* Reagent
* JSON RPC
* Postgres

## Usage

### Run backend
`lein uberjar`

`java -jar ...-standalone.jar`

### Run frontend

`shadow-cljs compile app`

`shadow-cljs server`

### TODO
* Docker and K8
* Build pipeline