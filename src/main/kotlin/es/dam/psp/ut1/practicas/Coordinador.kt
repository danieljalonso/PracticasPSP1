package es.dam.psp.ut1.practicas

import es.dam.psp.ut1.Jvm
import java.io.File
import kotlin.time.measureTime

/**
 * Resultado que devuelve un hijo para un fichero.
 */
data class Resultado(
    val fichero: String,
    val lineas: Long,
    val palabras: Long,
    val caracteres: Long,
    val masFrecuente: String
)

const val CLASE_HIJO = "es.dam.psp.ut1.practicas.ContadorKt"

/**
 * TODO 4.a
 * Crea y arranca un proceso Contador cuya entrada estándar sea el fichero.
 */
fun lanzarContador(fichero: File): Process {
    return Jvm.proceso(CLASE_HIJO)
        .redirectInput(fichero)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
}

/**
 * TODO 4.b
 * Lee el resultado del hijo y comprueba su código de salida.
 */
fun recogerResultado(fichero: File, hijo: Process): Resultado {
    val linea = hijo.inputStream.bufferedReader().use { it.readLine() }

    val codigoSalida = hijo.waitFor()

    if (codigoSalida != 0) {
        throw IllegalStateException(
            "El contador del fichero ${fichero.name} terminó con código $codigoSalida"
        )
    }

    if (linea == null) {
        throw IllegalStateException(
            "El contador del fichero ${fichero.name} no devolvió ningún resultado"
        )
    }

    val partes = linea.split(";")

    if (partes.size != 4) {
        throw IllegalStateException(
            "Resultado inválido para ${fichero.name}: $linea"
        )
    }

    return Resultado(
        fichero = fichero.name,
        lineas = partes[0].toLong(),
        palabras = partes[1].toLong(),
        caracteres = partes[2].toLong(),
        masFrecuente = partes[3]
    )
}

/**
 * TODO 4.c
 * Procesa los ficheros uno detrás de otro.
 */
fun secuencial(ficheros: List<File>): List<Resultado> {
    return ficheros.map { fichero ->
        val hijo = lanzarContador(fichero)
        recogerResultado(fichero, hijo)
    }
}

/**
 * TODO 4.d
 * Lanza todos los hijos y después recoge sus resultados.
 */
fun concurrente(ficheros: List<File>): List<Resultado> {
    val hijos = ficheros.map { fichero ->
        fichero to lanzarContador(fichero)
    }

    return hijos.map { (fichero, hijo) ->
        recogerResultado(fichero, hijo)
    }
}

/**
 * TODO 4.e
 * Muestra los resultados y comprueba que ambos modos coinciden.
 */
fun main() {
    val ficheros = File("datos")
        .listFiles { f -> f.extension == "txt" }
        ?.sortedBy { it.name }

    if (ficheros.isNullOrEmpty()) {
        println("No hay ficheros en datos/. Ejecuta antes GeneradorDatos.kt")
        return
    }

    println("Procesadores disponibles: ${Runtime.getRuntime().availableProcessors()}")

    lateinit var r1: List<Resultado>
    lateinit var r2: List<Resultado>

    val t1 = measureTime {
        r1 = secuencial(ficheros)
    }

    val t2 = measureTime {
        r2 = concurrente(ficheros)
    }

    fun mostrarTabla(resultados: List<Resultado>) {
        println()
        println(
            "%-20s %12s %12s %14s %s".format(
                "FICHERO", "LÍNEAS", "PALABRAS", "CARACTERES", "MÁS FRECUENTE"
            )
        )
        println("-".repeat(85))

        resultados.forEach { r ->
            println(
                "%-20s %12d %12d %14d %s".format(
                    r.fichero,
                    r.lineas,
                    r.palabras,
                    r.caracteres,
                    r.masFrecuente
                )
            )
        }

        val totalLineas = resultados.sumOf { it.lineas }
        val totalPalabras = resultados.sumOf { it.palabras }
        val totalCaracteres = resultados.sumOf { it.caracteres }

        println("-".repeat(85))
        println(
            "%-20s %12d %12d %14d".format(
                "TOTAL",
                totalLineas,
                totalPalabras,
                totalCaracteres
            )
        )
    }

    println("\nRESULTADOS SECUENCIALES")
    mostrarTabla(r1)

    println("\nRESULTADOS CONCURRENTES")
    mostrarTabla(r2)

    // Se comparan los resultados de ambos modos.
    check(r1 == r2) {
        "ERROR: los resultados secuenciales y concurrentes no coinciden"
    }

    println("\nCorrecto: ambos modos producen los mismos resultados.")
    println("Secuencial:  $t1")
    println("Concurrente: $t2")

    val aceleracion = t1.inWholeNanoseconds.toDouble() /
            t2.inWholeNanoseconds.toDouble()

    println("Aceleración: %.2fx".format(aceleracion))
}
