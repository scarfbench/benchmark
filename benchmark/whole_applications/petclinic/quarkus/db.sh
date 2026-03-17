#!/usr/bin/env bash

printf "\n\n######## Deploying PostgreSQL ########\n"

kubectl apply -f src/main/kubernetes/pgsql.yml
kubectl wait --for=condition=available --timeout=60s deployment/postgresql

printf "\n\n######## Create Schema PostgreSQL ########\n"

kubectl apply -f src/main/kubernetes/pgsql-db-creator.yml
kubectl wait --for=condition=complete job/petclinic-schema