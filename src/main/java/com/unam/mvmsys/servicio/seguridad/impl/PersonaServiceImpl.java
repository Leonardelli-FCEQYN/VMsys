package com.unam.mvmsys.servicio.seguridad.impl;

import com.unam.mvmsys.entidad.financiero.CuentaCorriente;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.repositorio.seguridad.PersonaRepository;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepo;

    @Override
    @Transactional
    public Persona crearPersona(Persona persona) {
        // 1. Validar CUIT duplicado
        if (personaRepo.existsByCuitDni(persona.getCuitDni())) {
            throw new IllegalArgumentException("Ya existe una persona con el CUIT/DNI: " + persona.getCuitDni());
        }

        // 2. Regla de Negocio: Crear Cuenta Corriente automáticamente
        // Como la relación es CascadeType.ALL en Persona, basta con asignarla
        CuentaCorriente cc = CuentaCorriente.builder()
                .persona(persona)
                .saldoActual(BigDecimal.ZERO)
                .build();
        
        persona.setCuentaCorriente(cc);

        return personaRepo.save(persona);
    }

    @Override
    @Transactional
    public Persona actualizarPersona(Persona persona) {
        if (persona == null || persona.getId() == null) {
            throw new IllegalArgumentException("La persona y su ID no pueden ser nulos.");
        }
        
        @SuppressWarnings("null")
        boolean exists = personaRepo.existsById(persona.getId());
        if (!exists) {
            throw new IllegalArgumentException("La persona no existe.");
        }
        // Nota: Al actualizar, JPA mantiene la cuenta corriente existente
        return personaRepo.save(persona);
    }

    @Override
    @Transactional
    public void eliminarPersona(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la persona no puede ser nulo.");
        }
        
        personaRepo.findById(id).ifPresent(p -> {
            p.setActivo(false);
            personaRepo.save(p);
        });
    }

    @Override
    public List<Persona> listarTodas() {
        return personaRepo.findAll().stream()
                .filter(Persona::isActivo)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> listarClientes() {
        return listarTodas().stream()
                .filter(Persona::isEsCliente)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> listarProveedores() {
        return listarTodas().stream()
                .filter(Persona::isEsProveedor)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Persona> buscarPorCuit(String cuit) {
        return personaRepo.findByCuitDni(cuit);
    }
}