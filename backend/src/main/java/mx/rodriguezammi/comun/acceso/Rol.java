package mx.rodriguezammi.comun.acceso;

/**
 * Los dos niveles de acceso del sistema:
 *
 *   PERSONAL → mamá, papá e hija en el día a día (vender, clientes, armazones, impresiones).
 *              Se entra UNA vez por celular con un PIN y el celular queda "recordado".
 *   ADMIN    → el dueño: reportes de ventas (día, semana, mes) y configuración.
 *              Pide contraseña y la sesión dura pocas horas.
 *
 * Los clientes de la papelería NO tienen rol: solo pueden usar el buzón de impresión.
 */
public enum Rol {
    PERSONAL,
    ADMIN
}
