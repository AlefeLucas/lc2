import analyzer.Parser
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.system.exitProcess

const val USAGE: String = "Uso: java LC <arquivo fonte> <saida assembly>"

/**
 * Metodo principal - trata a parametrizacao e instancia o analisador sintático.
 *
 * @param args argumentos da linha de comando do programa.'
 */
fun main(args: Array<String>) {
    if (args.size != 2) {
        println(USAGE)
    } else {
        val sourceFilePath = args.first()
        val outputFilePath = args[1]
        val sourceFile = getSourceFile(sourceFilePath)
        val outputFile = getOutputFile(outputFilePath)

        if (sourceFile != null && outputFile != null) {
            try {
                val source = Files.readString(sourceFile.toPath(), StandardCharsets.US_ASCII)
                val parser = Parser(source)
                parser.parse()
            } catch (e: IOException) {
                println("Erro: falhou ao ler arquivo fonte.\n${e.message}")
                e.printStackTrace()
            }
        } else {
            exitProcess(1)
        }

    }
}

/**
 * Obtem o arquivo de saida de um dado caminho.
 *
 * @param outputFilePath caminho para o arquivo de saida.
 * @return objeto File se a criacao do arquivo foi bem sucedida, null caso contrario.
 */
fun getOutputFile(outputFilePath: String): File? {
    var output: File? = null
    if (validOutputName(outputFilePath)) {
        val file = File(outputFilePath)
        try {
            file.createNewFile()
            output = file
        } catch (e: IOException) {
            println("lc: Nao pode criar arquivo de saida.")
        }
    } else {
        println("lc: Formato de saida invalido. Use extensao assembly (.asm).")
    }
    return output
}

/**
 * Obtem um arquivo fonte de um dado caminho.
 *
 * @param sourceFilePath caminho do arquivo fonte.
 * @return objeto se um arquivo valido e encontrado, null caso contrario.
 */
fun getSourceFile(sourceFilePath: String): File? {
    var source: File? = null
    if (validSourceName(sourceFilePath)) {
        val file = File(sourceFilePath)
        if (validateSourceFile(file)) {
            source = file
        }
    } else {
        println("lc: Formato de entrado invalido. Use extensao .L")
    }
    return source
}

/**
 * Verifica se o arquivo fonte existe e eh legivel.
 */
fun validateSourceFile(sourceFile: File): Boolean {
    var validSourceFile = false
    if (sourceFile.exists()) {
        if (!sourceFile.canRead()) {
            println("lc: Nao pode ler de ${sourceFile.path}")
        } else {
            validSourceFile = true
        }
    } else {
        println("lc: arquivo nao encontrado: ${sourceFile.path}")
        println(USAGE)
    }
    return validSourceFile
}

/**
 * Verifica se a string contem um nome de arquivo de saida valido (terminando em ".asm" case insensitive)
 */
fun validOutputName(outputFileName: String): Boolean = outputFileName.matches(".*[.][aA][sS][mM]$".toRegex())


/**
 * Verifica se a string contem um nome de arquivo fonte valido (terminando em ".l" ou ".L")
 */
fun validSourceName(sourceFilePath: String): Boolean = sourceFilePath.matches(".*[.][lL]$".toRegex())

