package android.webkit

class TestSslErrorHandler : SslErrorHandler() {
    var cancelled = false
        private set
    var proceeded = false
        private set

    override fun cancel() {
        cancelled = true
    }

    override fun proceed() {
        proceeded = true
    }
}
