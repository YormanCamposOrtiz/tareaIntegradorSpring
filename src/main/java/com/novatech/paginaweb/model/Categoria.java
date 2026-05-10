package com.novatech.paginaweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String emoji; // Ejemplo: "💊", "🧼", "🍼"

    public Categoria() {}

    // Getters y Setters
}