package com.logistica.repositorio;

import java.time.LocalDateTime;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import com.logistica.database.Neo4jConexion;

public class EnvioGrafoRepository {

    // nodo envio + relaciones tiene, origen y destino.
    public void crearEnvio(int envioId, String estado, LocalDateTime fechaCreacion, double pesoKg, int clienteId,
            String clienteNombre,
            int origenId, String origenNombre, String origenPais, int destinoId, String destinoNombre,
            String destinoPais) {

        String cypher = """
                MERGE (c:Cliente {id: $clienteId})
                SET c.razonSocial = $clienteNombre
                MERGE (o:Ubicacion {id: $origenId})
                SET o.nombre = $origenNombre, o.pais = $origenPais
                MERGE (d:Ubicacion {id: $destinoId})
                SET d.nombre = $destinoNombre, d.pais = $destinoPais
                MERGE (e:Envio {id: $envioId})
                SET e.estado = $estado, e.fechaCreacion = $fechaCreacion, e.pesoKg = $pesoKg
                MERGE (c)-[:TIENE]->(e)
                MERGE (e)-[:ORIGEN]->(o)
                MERGE (e)-[:DESTINO]->(d)
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            session.run(cypher, Values.parameters(
                    "envioId", envioId, "estado", estado,
                    "fechaCreacion", fechaCreacion, "pesoKg", pesoKg,
                    "clienteId", clienteId, "clienteNombre", clienteNombre,
                    "origenId", origenId, "origenNombre", origenNombre, "origenPais", origenPais,
                    "destinoId", destinoId, "destinoNombre", destinoNombre, "destinoPais", destinoPais))
                    .consume();
        }
    }

    public void borrarEnvio(int envioId) {
        try (Session session = Neo4jConexion.getDriver().session()) {
            session.run("MATCH (e:Envio {id: $envioId}) DETACH DELETE e",
                    Values.parameters("envioId", envioId)).consume();
        }
    }

    public void actualizarEnvio(int envioId, String estado, double pesoKg) {
        String cypher = """
                MATCH (e:Envio {id: $envioId})
                SET e.estado = $estado, e.pesoKg = $pesoKg
                RETURN e.id
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            Result r = session.run(cypher,
                    Values.parameters("envioId", envioId, "estado", estado, "pesoKg", pesoKg));
            verificarQueExiste(r, envioId);
        }
    }

    public void asignarContenedor(int envioId, int contenedorId, String codigo, LocalDateTime fecha) {
        String cypher = """
                MATCH (e:Envio {id: $envioId})
                MERGE (k:Contenedor {id: $contenedorId})
                SET k.codigo = $codigo
                MERGE (e)-[u:UTILIZA]->(k)
                SET u.fechaAsignacion = $fecha
                RETURN e.id
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            Result r = session.run(cypher, Values.parameters(
                    "envioId", envioId, "contenedorId", contenedorId, "codigo", codigo, "fecha", fecha));
            verificarQueExiste(r, envioId);
        }
    }

    public void quitarContenedor(int envioId, int contenedorId) {
        String cypher = """
                MATCH (:Envio {id: $envioId})-[u:UTILIZA]->(:Contenedor {id: $contenedorId})
                DELETE u
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            session.run(cypher, Values.parameters("envioId", envioId, "contenedorId", contenedorId))
                    .consume();
        }
    }

    // relaciones pasa_por (envio -> ciudad destino del tramo) y gestionado_por
    // (envio -> operador).
    public void agregarTramo(int envioId, int tramoId, int orden, String medio,
            int destinoId, String destinoNombre, String destinoPais,
            int operadorId, String operadorNombre,
            LocalDateTime fechaSalida, LocalDateTime fechaLlegada, String estado) {

        String cypher = """
                MATCH (e:Envio {id: $envioId})
                MERGE (u:Ubicacion {id: $destinoId})
                SET u.nombre = $destinoNombre, u.pais = $destinoPais
                MERGE (op:Operador {id: $operadorId})
                SET op.razonSocial = $operadorNombre
                MERGE (e)-[p:PASA_POR {tramoId: $tramoId}]->(u)
                SET p.orden = $orden, p.medio = $medio, p.operador = $operadorNombre,
                    p.fechaSalida = $fechaSalida, p.fechaLlegada = $fechaLlegada, p.estado = $estado
                MERGE (e)-[g:GESTIONADO_POR {tramoId: $tramoId}]->(op)
                RETURN e.id
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            Result r = session.run(cypher, Values.parameters(
                    "envioId", envioId, "tramoId", tramoId, "orden", orden, "medio", medio,
                    "destinoId", destinoId, "destinoNombre", destinoNombre, "destinoPais", destinoPais,
                    "operadorId", operadorId, "operadorNombre", operadorNombre,
                    "fechaSalida", fechaSalida, "fechaLlegada", fechaLlegada, "estado", estado));
            verificarQueExiste(r, envioId);
        }
    }

    public void actualizarTramo(int tramoId, String estado, LocalDateTime fechaSalida,
            LocalDateTime fechaLlegada) {

        String cypher = """
                MATCH (:Envio)-[p:PASA_POR {tramoId: $tramoId}]->(:Ubicacion)
                SET p.estado = $estado, p.fechaSalida = $fechaSalida, p.fechaLlegada = $fechaLlegada
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            session.run(cypher, Values.parameters("tramoId", tramoId, "estado", estado,
                    "fechaSalida", fechaSalida, "fechaLlegada", fechaLlegada)).consume();
        }
    }

    public void borrarTramo(int tramoId) {
        try (Session session = Neo4jConexion.getDriver().session()) {
            session.run("MATCH (:Envio)-[r {tramoId: $tramoId}]->() DELETE r",
                    Values.parameters("tramoId", tramoId)).consume();
        }
    }

    private void verificarQueExiste(Result r, int envioId) {
        if (!r.hasNext()) {
            throw new IllegalStateException("El envio " + envioId + " no existe en Neo4j.");
        }
        r.consume();
    }

}
