package com.novatech.paginaweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.novatech.paginaweb.repository.UsuarioRepository;

import java.util.List;
import java.util.ArrayList;



@RestController 
@RequestMapping("/api") 
// Asegúrate de que el puerto 5173 sea el que usa tu Vite
@CrossOrigin(origins = "http://localhost:5173") 
public class BaseController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/inicio/datos")
    public InicioData getDatosInicio() {
        // Simulamos la respuesta que consumirá tu componente Home (Inicio)
        List<CategoriaDTO> categorias = new ArrayList<>();
        categorias.add(new CategoriaDTO("Medicamentos", "💊", "bg-blue-100", 234));
        categorias.add(new CategoriaDTO("Vitaminas", "🌟", "bg-orange-100", 156));
        categorias.add(new CategoriaDTO("Cuidado Personal", "🧴", "bg-pink-100", 189));
        categorias.add(new CategoriaDTO("Bebé", "👶", "bg-green-100", 98));

        return new InicioData(categorias, "Bienvenido a SaludPlus");
    }

    
    static class InicioData {
        public List<CategoriaDTO> categorias;
        public String mensaje;
        public InicioData(List<CategoriaDTO> categorias, String mensaje) {
            this.categorias = categorias;
            this.mensaje = mensaje;
        }
    }

    static class CategoriaDTO {
        public String name;
        public String icon;
        public String color;
        public int count;
        
        public CategoriaDTO(String name, String icon, String color, int count) {
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.count = count;
        }
    }
}