package android.webkit;

/** Test-only handler because the framework handler constructor is package-private. */
public final class TestSslErrorHandler extends SslErrorHandler {
    public boolean cancelled;
    public boolean proceeded;

    public TestSslErrorHandler() {
        super();
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public void proceed() {
        proceeded = true;
    }
}
