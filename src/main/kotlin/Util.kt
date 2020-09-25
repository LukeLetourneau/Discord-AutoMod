import java.io.File
import java.io.PrintWriter

fun getWriter(filename: String) = PrintWriter(File(Bot::javaClass.javaClass.getResource(filename).path))

fun getReader(filename: String) = Bot::javaClass.javaClass.getResourceAsStream(filename).reader()