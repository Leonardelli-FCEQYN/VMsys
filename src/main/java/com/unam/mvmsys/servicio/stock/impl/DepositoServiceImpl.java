package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.Deposito;
import com.unam.mvmsys.repositorio.stock.DepositoRepository;
import com.unam.mvmsys.servicio.stock.DepositoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositoServiceImpl implements DepositoService {

    private final DepositoRepository depositoRepository;

    @Override
    @Transactional
    public Deposito crearDeposito(Deposito deposito) {
        if (deposito == null || deposito.getNombre() == null || deposito.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del depósito es obligatorio");
        }

        if (depositoRepository.findByNombre(deposito.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un depósito con el nombre: " + deposito.getNombre());
        }

        deposito.setActivo(true);
        return depositoRepository.save(deposito);
    }

    @Override
    @Transactional
    public Deposito actualizarDeposito(Deposito deposito) {
        if (deposito == null || deposito.getId() == null) {
            throw new IllegalArgumentException("Depósito inválido");
        }

        Deposito existente = depositoRepository.findById(deposito.getId())
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado"));

        existente.setNombre(deposito.getNombre());
        existente.setDireccion(deposito.getDireccion());
        existente.setEsPropio(deposito.isEsPropio());

        return depositoRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminarDeposito(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID de depósito no válido");
        }

        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado"));

        deposito.setActivo(false);
        depositoRepository.save(deposito);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deposito> listarTodos() {
        return depositoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deposito> listarActivos() {
        return depositoRepository.findAllActivos();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Deposito> buscarPorId(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return depositoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Deposito> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        return depositoRepository.findByNombre(nombre);
    }
}
