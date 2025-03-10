package org.example.jdbcrestaurantecrud.models;


public class Producto {

    private Integer id_producto;
    private String nombre;
    private String categoria;
    private float precio;
    private int disponibilidad;

    public Producto() {}

    public Producto(int id_producto, String nombre, String categoria, float precio, int disponibilidad) {
        this.id_producto = id_producto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.disponibilidad = disponibilidad;
    }

    public Producto(int idProducto, String nombreProducto, float precioProducto) {
        this.id_producto = idProducto;
        this.nombre = nombreProducto;
        this.precio = precioProducto;
    }


    public Integer getId_producto() {
        return id_producto;
    }

    public void setId_producto(Integer id_producto) {
        this.id_producto = id_producto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public int getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(int disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

}