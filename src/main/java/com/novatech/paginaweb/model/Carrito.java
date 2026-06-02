package com.novatech.paginaweb.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "carrito_id")
    private List<ItemCarrito> items = new ArrayList<>();

    private Double total;

    public Carrito() {}


    public void agregarItem(ItemCarrito item) {
        this.items.add(item);
    }


    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getUuid() { 
        return uuid; 
    }
    public void setUuid(String uuid) { 
        this.uuid = uuid; 
    }

    public List<ItemCarrito> getItems() { 
        return items; 
    }
    public void setItems(List<ItemCarrito> items) { 
        this.items = items; 
    }

    public Double getTotal() { 
        return total; 
    }
    public void setTotal(Double total) { 
        this.total = total; 
    }
}
