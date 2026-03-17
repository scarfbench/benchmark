# Concurrency Jobs Example (Open Liberty)

This module demonstrates usage of Managed Executor Services (MES_High and MES_Low) using Jakarta EE Concurrency on Open Liberty.

## Run on Open Liberty

Prerequisites: Java 11+ and Maven.

Build and start the Liberty server (dev mode hot reload):
```
./mvnw liberty:dev
```
The application will be available at:
- http://localhost:9080/
- REST endpoints (if any registered) under http://localhost:9080/webapi/

Stop dev mode with `q` then Enter.

To run a packaged server:
```
./mvnw package liberty:run
```

## Managed Executor Services Configuration
Defined in `src/main/liberty/config/server.xml`:
```
<managedExecutorService id="MES_High" jndiName="MES_High" coreThreads="5" maxThreads="10"/>
<managedExecutorService id="MES_Low"  jndiName="MES_Low"  coreThreads="2" maxThreads="4"/>
```
They are referenced in `web.xml` as resource-env-ref entries.

Adjust thread pool sizes in `server.xml` as needed.

## Rebuild / Clean
```
./mvnw clean package
```
Resulting WAR: `target/jobs.war` (deployed automatically in dev mode).

## Legacy: GlassFish Admin Commands (Deprecated)
If you previously used GlassFish, equivalent creation/deletion commands were:
```
$GF_HOME/glassfish/bin/asadmin create-managed-executor-service  --threadpriority 10 --corepoolsize 2 --maximumpoolsize 5  --taskqueuecapacity 2 MES_High
$GF_HOME/glassfish/bin/asadmin create-managed-executor-service  --threadpriority 1 --corepoolsize 1 --maximumpoolsize 1  --taskqueuecapacity 0 MES_Low
$GF_HOME/glassfish/bin/asadmin delete-managed-executor-service  MES_High
$GF_HOME/glassfish/bin/asadmin delete-managed-executor-service  MES_Low
```
These are no longer required because Open Liberty resources are declarative in `server.xml`.

## Notes
- Jakarta EE 10 API (`jakarta.jakartaee-api:10.0.0`) is used with feature `jakartaee-10.0`.
- Update `server.xml` for additional features (e.g., persistence) if needed.
