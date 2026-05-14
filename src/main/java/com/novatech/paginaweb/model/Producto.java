package com.novatech.paginaweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "visibilidad", nullable = false)
    private boolean visibilidad = true;

    @Column(name = "imagen", nullable = false)
    private String imagen;

    @Column(nullable = false)
    private Double precio_compra;

    @Column(nullable = false)
    private Double precio_venta;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_min")
    private Integer stockMin = 0;

    @ManyToOne
    @JoinColumn(name = "categoria_id") // Esto crea la columna en Neon
    private Categoria categoria;

    public Producto() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isVisibilidad() { return visibilidad; }
    public void setVisibilidad(boolean visibilidad) { this.visibilidad = visibilidad; }

    public Double getPrecio_compra() { return precio_compra; }
    public void setPrecio_compra(Double precio_compra) { this.precio_compra = precio_compra; }

    public Double getPrecio_venta() { return precio_venta; }
    public void setPrecio_venta(Double precio_venta) { this.precio_venta = precio_venta; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStockMin() { return stockMin; } // Cambiado a nombre estándar
    public void setStockMin(Integer stockMin) { this.stockMin = stockMin; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getImagen() { return imagen;}
    public void setImagen(String imagen) { this.imagen = imagen; }

}