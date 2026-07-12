
package com.novatech.paginaweb.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    @JsonIgnore
    private String contrasena;

    @Column(nullable = false)
    private String rol;

    // 🔍 NUEVOS CAMPOS AGREGADOS
    @Column(length = 255) // Permite guardar una dirección detallada (Calle, Distrito, etc.)
    private String direccion;

    @Column(length = 20) // Guarda el número telefónico como String para evitar pérdida de ceros a la izquierda
    private String telefono;

    @Column(length = 8) // Permite guardar el DNI (8 caracteres)
    private String dni;

    @Column(length = 255)
    private String apellidos; // Nuevo campo para el apellido del usuario

    // Campos de Seguridad
    @Column(name = "intentos_fallidos", columnDefinition = "INT DEFAULT 0")
    @JsonIgnore
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    @JsonIgnore
    private LocalDateTime bloqueadoHasta;
    // Constructor vacío requerido por JPA
    public Usuario() {
    }

    // Constructor actualizado para registro completo (incluye dirección y teléfono)
    public Usuario(String nombre, String apellidos, String correo, String contrasena, String rol, String direccion, String telefono, String dni) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.direccion = direccion;
        this.telefono = telefono;
        this.dni = dni;
        this.intentosFallidos = 0;
    }

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    // 🔍 Getters y Setters de los nuevos campos
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono ) { this.telefono = telefono; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public Integer getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
}