package es.dam.psp.ut1.practicas

/**
 * E6 · Un "ps" casero con ProcessHandle: información de los procesos del sistema.
 *
 * Apuntes UT-1, apartado 6.4. Base de la Práctica 2.c (practicas/BuscarProcesos.kt).
 * Cómo ejecutarlo: triángulo ▶ junto a main, sin argumentos.
 * Muestra el PID propio y el del padre, y una tabla PID, PPID, USUARIO y COMANDO con los
 * 15 primeros procesos visibles. Solo verás los datos que el sistema operativo permite leer
 * a tu usuario: cada dato de info() es un Optional que puede estar vacío.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Uso: BuscarProcesos <cadena>")
        return
    }

    val busqueda = args[0]

    println("%-8s %-8s %-12s %-25s %s".format(
        "PID", "PPID", "USUARIO", "INICIO", "CPU TOTAL"
    ))

    ProcessHandle.allProcesses()
        .filter { it.info().command().isPresent }
        .filter {
            it.info().command().get().contains(busqueda, ignoreCase = true)
        }
        .forEach { p ->
            val info = p.info()

            println(
                "%-8d %-8s %-12s %-25s %s".format(
                    p.pid(),
                    p.parent().map { it.pid().toString() }.orElse("-"),
                    info.user().orElse("?").takeLast(12),
                    info.startInstant().map { it.toString() }.orElse("?"),
                    info.totalCpuDuration().map { it.toString() }.orElse("?")
                )
            )
        }
}
