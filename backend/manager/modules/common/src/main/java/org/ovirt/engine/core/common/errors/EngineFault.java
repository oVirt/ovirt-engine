package org.ovirt.engine.core.common.errors;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Queryable;

public class EngineFault implements Queryable {
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

    private EngineError error;

    public EngineError getError() {
        return error;
    }

    public void setError(EngineError value) {
        error = value;
    }

    public int getErrorCode() {
        return error.getValue();
    }

    public void setError(int value) {
        error = EngineError.forValue(value);
    }

    public EngineFault(RuntimeException ex, EngineError error) {
        this(ex);
        setMessage(error.toString());
        setError(error);
    }

    public EngineFault(RuntimeException ex) {
        error = EngineError.unexpected;
        setMessage(ex.getMessage());

        // Pass over the inner exceptions and accumulate them within an array.
        setDetails(getInnerException(ex));
    }

    public EngineFault() {
        error = EngineError.unexpected;
    }

    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    private static ArrayList<String> getInnerException(Throwable ex) {
        ArrayList<String> result = new ArrayList<>();
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
