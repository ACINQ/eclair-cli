class DummyResultWriter : IResultWriter {
    var lastResult: String? = null
    var lastError: String? = null

    override fun writeSuccess(message: String) {
        lastResult = message
    }

    override fun writeError(message: String) {
        lastError = message
    }
}