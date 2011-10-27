package org.ovirt.engine.core.searchbackend;

public class valueValidationFunction {
    valueValidationFunction inner;

    public valueValidationFunction() {

    }

    public valueValidationFunction(valueValidationFunction inner) {
        this.inner = inner;
    }

    public boolean invoke(String field, String value) {
        if (inner != null) {
            return inner.invoke(field, value);
        } else {
            throw new RuntimeException("valueValidationFunction must be overridden");
        }
    }

}
