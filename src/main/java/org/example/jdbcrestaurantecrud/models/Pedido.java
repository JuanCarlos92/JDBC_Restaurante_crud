package org.example.jdbcrestaurantecrud.models;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public class Pedido {

    private Integer id_pedido;
    private Integer id_cliente;
    private Cliente cliente;
    private Date fecha_pedido;
    private Time hora_pedido;
    private float total;
    private String estado;
    private List<DetallePedido> detalles;

    public Pedido() {}

    public Pedido(int id_pedido, Cliente cliente, Date fecha_pedido, Time hora_pedido, float total, String estado, List<DetallePedido> detalles) {
        this.id_pedido = id_pedido;
        this.cliente = cliente;
        this.fecha_pedido = fecha_pedido;
        this.hora_pedido = hora_pedido;
        this.total = total;
        this.estado = estado;
        this.detalles = detalles;
    }
    public Pedido(int id_pedido){
        this.id_pedido = id_pedido;
    }

    public int getId_pedido() { return id_pedido; }
    public void setId_pedido(int id_pedido) { this.id_pedido = id_pedido; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public void setId_pedido(Integer id_pedido) {
        this.id_pedido = id_pedido;
    }
    public Integer getId_cliente() {
        return id_cliente;
    }
    public void setId_cliente(Integer id_cliente) {
        this.id_cliente = id_cliente;
    }
    public Date getFecha_pedido() { return fecha_pedido; }
    public void setFecha_pedido(Date fecha_pedido) { this.fecha_pedido = fecha_pedido; }
    public Time getHora_pedido() { return hora_pedido; }
    public void setHora_pedido(Time hora_pedido) { this.hora_pedido = hora_pedido; }
    public float getTotal() { return total; }
    public void setTotal(float total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}
