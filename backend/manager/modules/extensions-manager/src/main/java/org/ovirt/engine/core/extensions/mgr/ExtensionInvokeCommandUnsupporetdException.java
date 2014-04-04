package org.ovirt.engine.core.extensions.mgr;

import org.ovirt.engine.api.extensions.ExtMap;

class ExtensionInvokeCommandUnsupportedException extends ExtensionInvokeCommandException {
    public ExtensionInvokeCommandUnsupportedException(String message, ExtMap input, ExtMap output, Throwable cause) {
        super(message, input, output, cause);
    }
    public ExtensionInvokeCommandUnsupportedException(String message, ExtMap input, ExtMap output) {
        super(message, input, output);
    }
}
