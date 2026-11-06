package com.logistica.modelo;

import java.util.List;

public class RelacionRed {

    private Integer clienteId;
    private String cliente;
    private Integer operadorId;
    private String operador;
    private List<Integer> envioIds;

    public RelacionRed(Integer clienteId, String cliente, Integer operadorId, String operador,
            List<Integer> envioIds) {
        this.clienteId = clienteId;
        this.cliente = cliente;
        this.operadorId = operadorId;
        this.operador = operador;
        this.envioIds = envioIds;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public String getCliente() {
        return cliente;
    }

    public Integer getOperadorId() {
        return operadorId;
    }

    public String getOperador() {
        return operador;
    }

    public List<Integer> getEnvioIds() {
        return envioIds;
    }

    @Override
    public String toString() {
        return cliente + " → " + operador + ": " + envioIds.size() + " envios " + envioIds;
    }

}
