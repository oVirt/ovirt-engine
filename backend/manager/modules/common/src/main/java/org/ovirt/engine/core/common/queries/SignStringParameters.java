package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

public class SignStringParameters extends QueryParametersBase implements Serializable {

    private String string;

    public SignStringParameters() {
    }

    public SignStringParameters(String string) {
        super();
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

}
