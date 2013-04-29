package org.ovirt.engine.api.restapi.rsdl;

import java.util.HashMap;
import java.util.Map;

public class Signature implements Cloneable {
    private Map<Object, Object> mandatoryArguments = new HashMap<Object, Object>();
    private Map<Object, Object> optionalArguments = new HashMap<Object, Object>();

    public Map<Object, Object> getMandatoryArguments() {
        return mandatoryArguments;
    }
    public void setMandatoryArguments(Map<Object, Object> mandatoryArguments) {
        this.mandatoryArguments = mandatoryArguments;
    }
    public Map<Object, Object> getOptionalArguments() {
        return optionalArguments;
    }
    public void setOptionalArguments(Map<Object, Object> optionalArguments) {
        this.optionalArguments = optionalArguments;
    }
    public boolean isEmpty() {
        return mandatoryArguments.isEmpty() && optionalArguments.isEmpty();
    }
    public void addMandatoryArgument(Object name, Object type) {
        mandatoryArguments.put(name,type);
    }
    public void addOptionalArgument(Object name, Object type) {
        optionalArguments.put(name,type);
    }
    public Signature clone() {
        Signature clonedSignature = new Signature();
        Map<Object, Object> mandatoryArguments = new HashMap<Object, Object>();
        mandatoryArguments.putAll(getMandatoryArguments());
        Map<Object, Object> optionalArguments = new HashMap<Object, Object>();
        optionalArguments.putAll(getOptionalArguments());
        clonedSignature.setMandatoryArguments(mandatoryArguments);
        clonedSignature.setOptionalArguments(optionalArguments);
        return clonedSignature;
    }
}
