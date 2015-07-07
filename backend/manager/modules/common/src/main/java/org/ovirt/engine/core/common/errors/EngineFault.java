package org.ovirt.engine.core.common.errors;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;

public class EngineFault implements IVdcQueryable {
    private static final long serialVersionUID = -8004317251171749327L;
    private String privateSessionID;

    public String getSessionID() {
        return privateSessionID;
    }

    public void setSessionID(String value) {
        privateSessionID = value;
    }

    private ArrayList<String> privateDetails;

    public ArrayList<String> getDetails() {
        return privateDetails;
    }

    public void setDetails(ArrayList<String> value) {
        privateDetails = value;
    }

    private EngineError _Error;

    public EngineError getError() {
        return _Error;
    }

    public void setError(EngineError value) {
        _Error = value;
    }

    public int getErrorCode() {
        return _Error.getValue();
    }

    public void setError(int value) {
        _Error = EngineError.forValue(value);
    }

    public EngineFault(RuntimeException ex, EngineError error) {
        this(ex);
        setMessage(error.toString());
        setError(error);
    }

    public EngineFault(RuntimeException ex) {
        _Error = EngineError.unexpected;
        setMessage(ex.getMessage());

        // Pass over the inner exceptions and accumulate them within an array.
        setDetails(getInnerException(ex));
    }

    public EngineFault() {
        _Error = EngineError.unexpected;
    }

    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    private static ArrayList<String> getInnerException(Throwable ex) {
        ArrayList<String> result = new ArrayList<String>();
        while (ex.getCause() != null) {
            result.add(ex.getCause().getMessage());
            ex = ex.getCause();
        }
        return result;
    }

    @Override
    public Object getQueryableId() {
        return getMessage();
    }

}
