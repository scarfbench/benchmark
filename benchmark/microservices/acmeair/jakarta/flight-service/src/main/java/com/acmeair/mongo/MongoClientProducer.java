package com.acmeair.mongo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@ApplicationScoped
public class MongoClientProducer {

    @ConfigProperty(name = "mongo.host", defaultValue = "localhost")
    String mongoHost;

    @ConfigProperty(name = "mongo.port", defaultValue = "27017")
    String mongoPort;

    @ConfigProperty(name = "mongo.dbname", defaultValue = "")
    String mongoDbname;

    @ConfigProperty(name = "mongo.user.passwd", defaultValue = "")
    String mongoUserPasswd;

    @ConfigProperty(name = "mongo.options", defaultValue = "")
    String mongoOptions;

    @Produces
    @ApplicationScoped
    public MongoClient createMongoClient() {
        String connStr = "mongodb://" + mongoUserPasswd + mongoHost + ":" + mongoPort + "/" + mongoDbname + mongoOptions;
        return MongoClients.create(new ConnectionString(connStr));
    }

    public String getConnectionString() {
        return "mongodb://" + mongoUserPasswd + mongoHost + ":" + mongoPort + "/" + mongoDbname + mongoOptions;
    }
}
