package org.ovirt.engine.core.extensions.mgr;

import org.ovirt.engine.api.extensions.ExtMap;

class ExtensionInvokeCommandFailedException extends ExtensionInvokeCommandException {
    public ExtensionInvokeCommandFailedException(String message, ExtMap input, ExtMap output, Throwable cause) {
        super(message, input, output, cause);
    }
    public ExtensionInvokeCommandFailedException(String message, ExtMap input, ExtMap output) {
        super(message, input, output);
    }
}
