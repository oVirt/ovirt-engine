package org.ovirt.engine.core.extensions.mgr;

import org.apache.commons.logging.Log;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.Extension;

public class ExtensionProxy implements Extension {

    private Extension proxied;
    private ExtMap context;

    private void dumpMap(String prefix, ExtMap map) {
        Log logger = context.<Log> get(ExtensionsManager.TRACE_LOG_CONTEXT_KEY);
        if (logger.isDebugEnabled()) {
            logger.debug(prefix + " BEGIN");
            logger.debug(map.toString());
            logger.debug(prefix + " END");
        }
    }

    public ExtensionProxy(Extension proxied, ExtMap context) {
        this.proxied = proxied;
        this.context = context;
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
        try {
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
        }
        dumpMap("Invoke Output", output);
    }

    public ExtMap invoke(ExtMap input, boolean allowUnsupported, boolean allowFail) {
        ExtMap output = new ExtMap();
        invoke(input, output);

        String message = output.<String>get(Base.InvokeKeys.MESSAGE);
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
                    output.<Throwable>get(ExtensionsManager.CAUSE_OUTPUT_KEY)
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
