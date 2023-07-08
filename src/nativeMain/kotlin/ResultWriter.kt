interface IResultWriter {
    fun writeSuccess(message: EclairInfo)
    fun writeError(message: String)
}

class ConsoleResultWriter: IResultWriter {
    override fun writeSuccess(message: EclairInfo) {
        println("Success: $message")
    }

    override fun writeError(message: String) {
        println("Error: $message")
    }
}