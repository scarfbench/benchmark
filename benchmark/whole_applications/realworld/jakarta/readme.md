# ![RealWorld Example App](openliberty-realworld-logo.png)

> ### OpenLiberty + JPA codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

### [Demo](https://github.com/gothinkster/realworld)&nbsp;&nbsp;&nbsp;&nbsp;[RealWorld](https://github.com/gothinkster/realworld)

This codebase was created to demonstrate a fully fledged fullstack application built with OpenLiberty + MicroProfile +
JPA including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to
the [RealWorld](https://github.com/gothinkster/realworld) repo.

# Getting started

This codebase uses a PostgreSQL database to persist its data. Use the provided docker-compose.yaml to start the required
DB.

You will need JDK17 as a prerequisite and our current build tool of choice is Maven.

IntegrationTests are ran with MicroShed, using Testcontainers. Make sure you've configured a docker environment
according to this [reference](https://java.testcontainers.org/supported_docker_environment/)

* Clone this repo
* Run the provided `docker-compose.yaml` to have a working PostgreSQL DB
* Run `mvn liberty:dev` to start a development OpenLiberty server with live updates
