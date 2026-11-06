package com.logistica.modelo;

import java.math.BigDecimal;

public class DesempenioMensual {

    private String periodo;
    private Integer entregados;
    private Integer aTiempo;
    private BigDecimal tiempoPromedioHs;
    private BigDecimal demoraPromedioHs;

    public DesempenioMensual() {

    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Integer getEntregados() {
        return entregados;
    }

    public void setEntregados(Integer entregados) {
        this.entregados = entregados;
    }

    public Integer getATiempo() {
        return aTiempo;
    }

    public void setATiempo(Integer aTiempo) {
        this.aTiempo = aTiempo;
    }

    public BigDecimal getTiempoPromedioHs() {
        return tiempoPromedioHs;
    }

    public void setTiempoPromedioHs(BigDecimal tiempoPromedioHs) {
        this.tiempoPromedioHs = tiempoPromedioHs;
    }

    public BigDecimal getDemoraPromedioHs() {
        return demoraPromedioHs;
    }

    public void setDemoraPromedioHs(BigDecimal demoraPromedioHs) {
        this.demoraPromedioHs = demoraPromedioHs;
    }

    @Override
    public String toString() {
        return periodo + ": " + entregados + " entregados, " + aTiempo + " a tiempo (" + (entregados == 0 ? 0 : aTiempo * 100 / entregados) + " %) | tiempo promedio " + tiempoPromedioHs + " h | demora promedio de los atrasados " + (demoraPromedioHs == null ? "-" : demoraPromedioHs + " h");
    }

}
