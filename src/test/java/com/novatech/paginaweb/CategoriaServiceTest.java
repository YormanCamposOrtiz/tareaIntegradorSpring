package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.CategoriaRepository;
import com.novatech.paginaweb.model.Categoria;
import com.novatech.paginaweb.service.impl.CategoriaServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository; 

    @InjectMocks
    private CategoriaServiceImpl categoriaService; 

    @Test
    void testListarCategoriasCorrectamente() {
        // 1. ARRANGED: Datos simulados limpios (cumpliendo con SOLID) [cite: 6]
        Categoria cat1 = new Categoria();
        cat1.setId(1L);
        cat1.setNombre("Medicamentos");

        Categoria cat2 = new Categoria();
        cat2.setId(2L);
        cat2.setNombre("Cuidado Personal");

        // Definimos el comportamiento controlado del repositorio simulado
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(cat1, cat2));

        // 2. ACT: Ejecutamos el método real de tu CategoriaServiceImpl
        List<Categoria> resultado = categoriaService.listarTodas();

        // 3. ASSERT: Validamos los resultados esperados de la lógica de negocio [cite: 6]
        assertNotNull(resultado, "La lista de categorías no debería ser nula");
        assertEquals(2, resultado.size(), "Deberían retornar exactamente 2 categorías");
        assertEquals("Medicamentos", resultado.get(0).getNombre());
        assertEquals("Cuidado Personal", resultado.get(1).getNombre());

        // Verificamos que interactuó con la capa DAO exactamente una vez [cite: 6]
        verify(categoriaRepository, times(1)).findAll();
    }
}