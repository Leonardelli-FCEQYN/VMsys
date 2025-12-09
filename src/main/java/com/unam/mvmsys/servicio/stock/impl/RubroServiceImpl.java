package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.Rubro;
import com.unam.mvmsys.repositorio.stock.RubroRepository;
import com.unam.mvmsys.servicio.stock.RubroService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RubroServiceImpl implements RubroService {

    private final RubroRepository rubroRepository;

    @Override
    public Rubro crearRubro(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rubro no puede estar vacío");
        }
        
        String nombreNormalizado = nombre.trim();
        
        if (rubroRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe un rubro con el nombre: " + nombreNormalizado);
        }
        
        Rubro nuevoRubro = Rubro.builder()
                .nombre(nombreNormalizado)
                .build();
        
        return rubroRepository.save(nuevoRubro);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rubro> obtenerPorId(UUID id) {
        return rubroRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rubro> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        return rubroRepository.findByNombreIgnoreCase(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rubro> listarTodos() {
        return rubroRepository.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rubro> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }
        return rubroRepository.findByNombreContainingIgnoreCase(texto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        return rubroRepository.existsByNombreIgnoreCase(nombre.trim());
    }
}
