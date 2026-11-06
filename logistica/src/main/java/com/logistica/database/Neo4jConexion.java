package com.logistica.database;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConexion {

    private static final String NEO4J_Url = "bolt://localhost:7687";
    private static final String NEO4J_Usuario = "neo4j";
    private static final String NEO4J_Password = "logistica123";
    private static final Driver NEO4J_DRIVER = GraphDatabase.driver(NEO4J_Url, AuthTokens.basic(NEO4J_Usuario, NEO4J_Password));

    public static Driver getDriver() {
        return NEO4J_DRIVER;
    }

}