interface IResultWriter {
    fun writeSuccess(message: String)
    fun writeError(message: String)
}

class ConsoleResultWriter: IResultWriter {
    override fun writeSuccess(message: String) {
        println(message)
    }

    override fun writeError(message: String) {
        println("Error: $message")
    }
}