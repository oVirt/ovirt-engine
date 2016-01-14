package org.ovirt.engine.core.extensions.mgr;

import org.ovirt.engine.api.extensions.ExtMap;

class ExtensionInvokeCommandException extends RuntimeException {

    private ExtMap input;
    private ExtMap output;

    public ExtensionInvokeCommandException(String message, ExtMap input, ExtMap output, Throwable cause) {
        super(message, cause);
        this.input = input;
        this.output = output;
    }

    public ExtensionInvokeCommandException(String message, ExtMap input, ExtMap output) {
        this(message, input, output, null);
    }

    @Override
    public String toString() {
        return String.format(
                "Class: %s%n" +
                        "Input:%n" +
                        "%s%n" +
                        "Output:%n" +
                        "%s%n",
            getClass(),
            input,
            output
        );
    }

    public ExtMap getInput() {
        return input;
    }

    public ExtMap getOutput() {
        return output;
    }

}
