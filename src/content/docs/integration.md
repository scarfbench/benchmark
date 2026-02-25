---
title: "Integration"
---

# Integration

Integration technologies featuring Jakarta Batch processing, JMS messaging patterns, message-driven beans, JAX-WS web services, and Java Connector Architecture for enterprise system integration.

## Examples

- **batch-phonebilling** - Jakarta Batch API with ItemReader, ItemProcessor, ItemWriter, and chunk-oriented processing
- **batch-webserverlog** - Jakarta Batch with custom partitioning, checkpointing, and job orchestration
- **connectors-trading** - Java Connector Architecture resource adapter for EIS integration
- **connectors-traffic** - JCA inbound resource adapter with MessageEndpoint and WorkManager for async messaging
- **jaxws-hello-appclient** - JAX-WS web service client using `@WebServiceRef` in application client container
- **jaxws-hello-webclient** - JAX-WS web service client invocation from servlet using Service API
- **jms-clientmdbentity** - Message-driven beans with JPA entity persistence and JMS topics
- **jms-clientsessionmdb** - Message-driven bean consuming messages from stateless session bean
- **jms-durablesubscription** - Durable topic subscriptions with `@MessageDriven` and ActivationConfig properties
- **jms-simple** - JMS producer and consumer using `@Resource` injection for ConnectionFactory and Queue/Topic
- **jms-simplemessage** - Basic JMS message exchange with producer and consumer patterns
- **jms-transactedexample** - Transacted JMS sessions with message-driven beans and container-managed transactions
- **jms-websimplemessage** - JMS integration with web tier using managed beans for message sending/receiving
