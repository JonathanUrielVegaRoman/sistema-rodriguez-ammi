package mx.rodriguezammi.papeleria.producto;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 *  REPOSITORY = el ALMACENISTA. Es el único que entra a la bodega (base de datos).
 * ============================================================================
 *
 * Fíjate: es una INTERFAZ vacía... ¡y aun así funciona! Al heredar de JpaRepository,
 * Spring nos regala estos métodos sin escribir una sola línea de SQL:
 *
 *   save(producto)      → guarda o actualiza
 *   findById(id)        → busca por id
 *   findAll()           → trae todos
 *   deleteById(id)      → borra
 *   count()             → cuenta
 *
 * Y lo más mágico: si escribes un método con un NOMBRE que siga las reglas de Spring,
 * él solo deduce la consulta SQL. Ejemplos de abajo:
 *
 *   findByActivoTrueOrderByNombreAsc()
 *     → SELECT * FROM productos WHERE activo = true ORDER BY nombre ASC
 *
 *   findByNombreContainingIgnoreCaseAndActivoTrue("cuad")
 *     → ... WHERE LOWER(nombre) LIKE '%cuad%' AND activo = true
 *
 * Cuando la consulta es más rara, se escribe a mano con @Query (en JPQL, que es como
 * SQL pero usando los nombres de las CLASES y ATRIBUTOS de Java, no de las tablas).
 *
 * Nota: en otros proyectos verás "DAO" (Data Access Object). El Repository de Spring
 * Data ES el DAO; no hace falta tener los dos.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //                                                    ↑ entidad  ↑ tipo del id

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String texto);

    Optional<Producto> findByCodigoBarras(String codigoBarras);

    /**
     * Lee el producto y le pone un CANDADO hasta que termine la transacción.
     *
     * Problema que resuelve: dos celulares cobran la última pieza al mismo tiempo. Sin candado,
     * los dos leen "queda 1" y los dos venden. Con candado (en SQL: SELECT ... FOR UPDATE), el
     * segundo ESPERA a que el primero termine, y entonces ya lee "quedan 0" y se rechaza.
     * Lo demuestra la prueba VentasSimultaneasTest.
     *
     * Se usa SIEMPRE que se va a cambiar la existencia (vender, cancelar, resurtir, ajustar).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> bloquearPorId(@Param("id") Long id);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.servicio = false AND p.existencia <= p.stockMinimo ORDER BY p.existencia ASC")
    List<Producto> buscarPorResurtir();
}
