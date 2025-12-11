package com.unam.mvmsys.servicio.produccion;

import com.unam.mvmsys.datatransferobj.RecetaFX;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import com.unam.mvmsys.repositorio.produccion.ProcesoEstandarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcesoEstandarService {

    private final ProcesoEstandarRepository repository;

    public ProcesoEstandarService(ProcesoEstandarRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RecetaFX> obtenerTodasLasRecetas() {
        return repository.findAll().stream()
                .map(proceso -> {
                    RecetaFX recetaFX = new RecetaFX(proceso);
                    // Dentro de la transacción, cargamos las colecciones lazy
                    recetaFX.cargarColecciones(proceso);
                    return recetaFX;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcesoEstandar obtenerPorId(UUID id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public ProcesoEstandar guardar(ProcesoEstandar proceso) {
        return repository.save(proceso);
    }

    @Transactional
    public void eliminar(UUID id) {
        repository.deleteById(id);
    }
}
