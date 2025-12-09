package com.unam.mvmsys.servicio.configuracion;

import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaClienteService {

    /**
     * Crea una nueva categoría de cliente
     */
    CategoriaCliente crear(String nombre, String descripcion, String icono, String colorHex);

    /**
     * Actualiza una categoría existente
     */
    CategoriaCliente actualizar(UUID id, String nombre, String descripcion, String icono, String colorHex);

    /**
     * Actualiza una categoría existente (sobrecarga con objeto)
     */
    CategoriaCliente actualizar(CategoriaCliente categoria);

    /**
     * Activa o desactiva una categoría
     */
    CategoriaCliente toggleActiva(UUID id);

    /**
     * Obtiene una categoría por ID
     */
    Optional<CategoriaCliente> obtenerPorId(UUID id);

    /**
     * Busca por nombre
     */
    Optional<CategoriaCliente> buscarPorNombre(String nombre);

    /**
     * Lista solo categorías activas
     */
    List<CategoriaCliente> listarActivas();

    /**
     * Lista todas (incluyendo inactivas)
     */
    List<CategoriaCliente> listarTodas();

    /**
     * Búsqueda de texto
     */
    List<CategoriaCliente> buscar(String texto);

    /**
     * Verifica si existe con ese nombre
     */
    boolean existe(String nombre);

    /**
     * Elimina una categoría (si no tiene clientes vinculados)
     */
    void eliminar(UUID id);
}
