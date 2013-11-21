package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.CPU;

public class CPUValidatorTest {
    @Test(expected = WebApplicationException.class)
    public void validateEnumsWrongArchitecture() {
        CPU entity = new CPU();
        entity.setArchitecture("wrongarchitecture");
        new CPUValidator().validateEnums(entity);
    }

    @Test()
    public void validateEnums() {
        CPU entity = new CPU();
        entity.setArchitecture("X86_64");
        new CPUValidator().validateEnums(entity);
    }
}
