package com.logistica.repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import com.logistica.database.Neo4jConexion;
import com.logistica.modelo.ParadaRuta;
import com.logistica.modelo.RelacionRed;

public class GrafoConsultaRepository {

    public ParadaRuta buscarOrigen(int envioId) {
        String cypher = """
                MATCH (e:Envio {id: $envioId})-[:ORIGEN]->(o:Ubicacion)
                RETURN o.nombre AS ciudad, o.pais AS pais
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            Result r = session.run(cypher, Values.parameters("envioId", envioId));
            if (!r.hasNext()) {
                return null;
            }
            Record fila = r.next();
            ParadaRuta p = new ParadaRuta();
            p.setEnvioId(envioId);
            p.setOrden(0);
            p.setCiudad(fila.get("ciudad").asString());
            p.setPais(fila.get("pais").asString());
            return p;
        }
    }

    public List<ParadaRuta> listarTramos(int envioId) {
        String cypher = """
                MATCH (e:Envio {id: $envioId})-[p:PASA_POR]->(u:Ubicacion)
                RETURN p.tramoId AS tramoId, p.orden AS orden, u.nombre AS ciudad, u.pais AS pais,
                       p.medio AS medio, p.operador AS operador, p.fechaSalida AS fechaSalida,
                       p.fechaLlegada AS fechaLlegada, p.estado AS estado
                ORDER BY p.orden
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            List<ParadaRuta> tramos = new ArrayList<>();
            Result r = session.run(cypher, Values.parameters("envioId", envioId));
            while (r.hasNext()) {
                Record fila = r.next();
                ParadaRuta p = new ParadaRuta();
                p.setEnvioId(envioId);
                p.setTramoId(fila.get("tramoId").asInt());
                p.setOrden(fila.get("orden").asInt());
                p.setCiudad(fila.get("ciudad").asString());
                p.setPais(fila.get("pais").asString());
                p.setMedio(fila.get("medio").asString());
                p.setOperador(fila.get("operador").asString());
                p.setFechaSalida(fecha(fila.get("fechaSalida")));
                p.setFechaLlegada(fecha(fila.get("fechaLlegada")));
                p.setEstado(fila.get("estado").asString());
                tramos.add(p);
            }
            return tramos;
        }
    }

    public List<Integer> listarEnviosDeContenedor(int contenedorId) {
        String cypher = """
                MATCH (k:Contenedor {id: $contenedorId})<-[u:UTILIZA]-(e:Envio)
                RETURN e.id AS envioId
                ORDER BY u.fechaAsignacion
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            List<Integer> envios = new ArrayList<>();
            Result r = session.run(cypher, Values.parameters("contenedorId", contenedorId));
            while (r.hasNext()) {
                envios.add(r.next().get("envioId").asInt());
            }
            return envios;
        }
    }

    public List<RelacionRed> redRelaciones(Integer clienteId, Integer operadorId) {
        String cypher = """
                MATCH (c:Cliente)-[:TIENE]->(e:Envio)-[:GESTIONADO_POR]->(o:Operador)
                WHERE ($clienteId IS NULL OR c.id = $clienteId)
                  AND ($operadorId IS NULL OR o.id = $operadorId)
                RETURN c.id AS clienteId, c.razonSocial AS cliente, o.id AS operadorId,
                       o.razonSocial AS operador, collect(DISTINCT e.id) AS envios
                ORDER BY cliente, operador
                """;

        try (Session session = Neo4jConexion.getDriver().session()) {
            List<RelacionRed> red = new ArrayList<>();
            Result r = session.run(cypher, Values.parameters("clienteId", clienteId, "operadorId", operadorId));
            while (r.hasNext()) {
                Record fila = r.next();
                red.add(new RelacionRed(fila.get("clienteId").asInt(), fila.get("cliente").asString(),
                        fila.get("operadorId").asInt(), fila.get("operador").asString(),
                        fila.get("envios").asList(Value::asInt)));
            }
            return red;
        }
    }

    private LocalDateTime fecha(Value v) {
        return v.isNull() ? null : v.asLocalDateTime();
    }

}
