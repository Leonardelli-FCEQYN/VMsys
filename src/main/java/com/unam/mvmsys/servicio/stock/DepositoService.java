package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.Deposito;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositoService {
    
    Deposito crearDeposito(Deposito deposito);
    
    Deposito actualizarDeposito(Deposito deposito);
    
    void eliminarDeposito(UUID id); // Desactivado lógico (Soft Delete)
    
    List<Deposito> listarTodos();
    
    List<Deposito> listarActivos();
    
    Optional<Deposito> buscarPorId(UUID id);
    
    Optional<Deposito> buscarPorNombre(String nombre);
}
