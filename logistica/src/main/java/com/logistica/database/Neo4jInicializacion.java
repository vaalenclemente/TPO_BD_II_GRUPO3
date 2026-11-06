package com.logistica.database;
import org.neo4j.driver.Session;

public class Neo4jInicializacion {

    private static final String[] INDICES = {
            "CREATE INDEX index_cliente_id IF NOT EXISTS FOR (c:Cliente) ON (c.id)",
            "CREATE INDEX index_operador_id IF NOT EXISTS FOR (o:Operador) ON (o.id)",
            "CREATE INDEX index_envio_id IF NOT EXISTS FOR (e:Envio) ON (e.id)",
            "CREATE INDEX index_contenedor_id IF NOT EXISTS FOR (c:Contenedor) ON (c.id)",
            "CREATE INDEX index_ubicacion_id IF NOT EXISTS FOR (u:Ubicacion) ON (u.id)"
    };

    public static void inicializar() {

        try (Session session = Neo4jConexion.getDriver().session()) {
            for (String cypher : INDICES) {
                session.run(cypher);
            }
        }
    }

}