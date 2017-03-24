package org.ovirt.engine.core.extensions.mgr;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.Extension;
import org.slf4j.Logger;

public class ExtensionProxy implements Extension {

    private final ClassLoader classLoader;
    private final Extension proxied;
    private final ExtMap context;

    private void dumpMap(String prefix, ExtMap map) {
        Logger logger = context.get(ExtensionsManager.TRACE_LOG_CONTEXT_KEY);
        if (logger.isTraceEnabled()) {
            logger.trace(prefix + " BEGIN");
            logger.trace(map.toString());
            logger.trace(prefix + " END");
        }
    }

    public ExtensionProxy(ClassLoader classLoader, Extension proxied) {
        this.classLoader = classLoader;
        this.proxied = proxied;
        this.context = new ExtMap();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Extension getExtension() {
        return proxied;
    }

    public ExtMap getContext() {
        return context;
    }

    @Override
    public void invoke(ExtMap input, ExtMap output) {
        input.putIfAbsent(Base.InvokeKeys.CONTEXT, context);

        dumpMap("Invoke Input", input);
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            proxied.invoke(input, output);
        } catch (Throwable e) {
            output.mput(
                Base.InvokeKeys.RESULT,
                Base.InvokeResult.FAILED
            ).mput(
                Base.InvokeKeys.MESSAGE,
                String.format(
                    "Exception: %s: %s",
                    e.getClass(),
                    e.getMessage()
                )
            ).mput(
                ExtensionsManager.CAUSE_OUTPUT_KEY,
                e
            );
        } finally {
            Thread.currentThread().setContextClassLoader(savedClassLoader);
        }
        dumpMap("Invoke Output", output);
    }

    public ExtMap invoke(ExtMap input, boolean allowUnsupported, boolean allowFail) {
        ExtMap output = new ExtMap();
        invoke(input, output);

        String message = output.get(Base.InvokeKeys.MESSAGE);
        switch(output.<Integer>get(Base.InvokeKeys.RESULT, Base.InvokeResult.FAILED)) {
        case Base.InvokeResult.SUCCESS:
            break;
        case Base.InvokeResult.UNSUPPORTED:
            if (!allowUnsupported) {
                throw new ExtensionInvokeCommandUnsupportedException(
                    message == null ? "Unsupported command" : message,
                    input,
                    output
                );
            }
            break;
        case Base.InvokeResult.FAILED:
        default:
            if (!allowFail) {
                throw new ExtensionInvokeCommandFailedException(
                    message == null ? "Invoke failed" : message,
                    input,
                    output,
                    output.get(ExtensionsManager.CAUSE_OUTPUT_KEY)
                );
            }
            break;
        }

        return output;
    }

    public ExtMap invoke(ExtMap input, boolean allowUnsupported) {
        return invoke(input, allowUnsupported, false);
    }

    public ExtMap invoke(ExtMap input) {
        return invoke(input, false, false);
    }

}
