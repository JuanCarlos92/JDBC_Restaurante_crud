package org.example.jdbcrestaurantecrud.models;

public class DetallePedido {
    private Integer id_detalle;
    private Pedido pedido;
    private Producto producto;
    private int cantidad;
    private float precio;
    private float subtotal;

    public DetallePedido() {}

    public DetallePedido(int id_pedido, Pedido pedido, Producto producto, int cantidad, float precio) {
        this.id_detalle = id_pedido;
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = cantidad * precio;
    }

    public DetallePedido(int idDetalle, Pedido pedido, Producto producto, int cantidad, float precioProducto, float subtotal) {
        this.id_detalle = idDetalle;
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precioProducto;
        this.subtotal = subtotal;
    }

    public int getId_detalle() { return id_detalle; }
    public void setId_detalle(int id_detalle) { this.id_detalle = id_detalle; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = cantidad * precio; // Recalcular subtotal
    }
    public float getPrecio() { return precio; }
    public void setPrecio(float precio) { this.precio = precio; }
    public float getSubtotal() { return subtotal; }
}
