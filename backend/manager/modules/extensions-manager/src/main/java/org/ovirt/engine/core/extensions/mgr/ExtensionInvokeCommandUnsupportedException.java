package org.ovirt.engine.core.extensions.mgr;

import org.ovirt.engine.api.extensions.ExtMap;

class ExtensionInvokeCommandUnsupporetdException extends ExtensionInvokeCommandException {
    public ExtensionInvokeCommandUnsupporetdException(String message, ExtMap input, ExtMap output, Throwable cause) {
        super(message, input, output, cause);
    }
    public ExtensionInvokeCommandUnsupporetdException(String message, ExtMap input, ExtMap output) {
        super(message, input, output);
    }
}
