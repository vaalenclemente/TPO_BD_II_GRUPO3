package com.logistica.database;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongodbConexion {

    private static final String MONGODB_Url = "mongodb://localhost:27017";
    private static final String MONGODB_Database = "logistica";
    private static final MongoClient MONGODB_CLIENTE = MongoClients.create(MONGODB_Url);

    public static MongoDatabase getDatabase(){
        return MONGODB_CLIENTE.getDatabase(MONGODB_Database); 
    }
}
