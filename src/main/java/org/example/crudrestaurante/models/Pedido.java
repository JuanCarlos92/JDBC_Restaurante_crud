package org.example.crudrestaurante.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Pedido {

    private Integer id;
    private Cliente cliente;
    private LocalDate fechaPedido;
    private LocalTime horaPedido;
    private float total;
    private String estado;
    private List<DetallePedido> detalles;

    public Pedido() {}

    public Pedido(int id, Cliente cliente, LocalDate fechaPedido, LocalTime horaPedido, float total, String estado, List<DetallePedido> detalles) {
        this.id = id;
        this.cliente = cliente;
        this.fechaPedido = fechaPedido;
        this.horaPedido = horaPedido;
        this.total = total;
        this.estado = estado;
        this.detalles = detalles;
    }
    public Pedido(int id){
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDate fechaPedido) { this.fechaPedido = fechaPedido; }
    public LocalTime getHoraPedido() { return horaPedido; }
    public void setHoraPedido(LocalTime horaPedido) { this.horaPedido = horaPedido; }
    public float getTotal() { return total; }
    public void setTotal(float total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}
