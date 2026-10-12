package es.dam.psp.ut1.practicas

import es.dam.psp.ut1.Jvm
import java.io.File
import java.util.concurrent.TimeUnit

// Necesitarás además: import java.io.File y import java.util.concurrent.TimeUnit

/**
 * Lanzador.kt · Práctica 3 · Lanzador de órdenes. Completa los TODO 3.x (letras de la práctica).
 *
 * Apuntes UT-1, apartados 5.1 (ProcessBuilder, Jvm.comandoShell), 5.3 (leer la salida),
 * 6.1 (waitFor con límite, destroy) y 6.2 (no bloquearse leyendo).
 * Cómo ejecutarlo: triángulo ▶ junto a main, sin argumentos; escribe las órdenes en la consola
 * de IntelliJ (p. ej. "echo hola", "ls noexiste", "sleep 10" o, en Windows, "ping -n 11 127.0.0.1").
 *
 * Bucle: pide una orden al usuario, la ejecuta como proceso hijo y muestra
 *   - su salida (stdout y stderr juntas), con cada línea numerada (3.a),
 *   - su código de salida y el tiempo que ha tardado en milisegundos (3.b).
 * Si la orden tarda más de TIMEOUT_S segundos, se termina el proceso y se avisa (3.c).
 * Cada ejecución se añade a historial.txt con el formato:  orden;codigo;milisegundos (3.d)
 * La orden "salir" termina el programa.
 */

const val TIMEOUT_S = 5L   // segundos que se deja trabajar a cada orden

fun main() {
    println("Lanzador PSP · sistema: ${System.getProperty("os.name")} · escribe 'salir' para terminar")
    while (true) {
        print("> ")
        val orden = readlnOrNull()?.trim() ?: break   // null = fin de la entrada (Ctrl+D)
        if (orden == "salir") break
        if (orden.isEmpty()) continue

        val tempFile = File.createTempFile("temp", ".txt")
        val pb = ProcessBuilder(Jvm.comandoShell(orden)).redirectErrorStream(true).redirectOutput(tempFile)

        val inicio = System.nanoTime()
        val proceso = pb.start()

        val terminoATiempo = proceso.waitFor(TIMEOUT_S, TimeUnit.SECONDS)
        if (!terminoATiempo) {
            println("Se han agotado los $TIMEOUT_S segundos")
            proceso.descendants().forEach { it.destroy() }
            proceso.destroy()
            if (proceso.isAlive) proceso.destroyForcibly()
        }

        val ms = (System.nanoTime() - inicio) / 1_000_000
        val codigoPrograma = if (terminoATiempo) proceso.exitValue() else -1

        val lineas = tempFile.readLines()

        lineas.forEachIndexed { i, line -> println("${i + 1} : $line") }
        tempFile.delete()
        println("[codigo $codigoPrograma · $ms ms]")

        File("historial.txt").appendText("$orden;$codigoPrograma;$ms\n")

    }
    println("Hasta luego")
}
