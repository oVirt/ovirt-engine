package org.ovirt.engine.core.common.queries;


public class GetValueBySessionQueryParameters extends VdcQueryParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = 6548937521017309017L;
    private String key;

    public GetValueBySessionQueryParameters() {
    }

    public GetValueBySessionQueryParameters(String sessionId, String key) {
        this.key = key;
        setSessionId(sessionId);
    }

    public String getKey() {
        return key;
    }

}
