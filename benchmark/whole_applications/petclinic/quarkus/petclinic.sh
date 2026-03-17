#!/usr/bin/env bash

printf "\n\n######## Deploying Quarkus Petclinic ########\n"

kubectl apply -f src/main/kubernetes/deployment.yml
kubectl wait --for=condition=available --timeout=60s deployment/quarkus-petclinic

kubectl get services