package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Cpu;

public class CPUValidatorTest {
    @Test(expected = WebApplicationException.class)
    public void validateEnumsWrongArchitecture() {
        Cpu entity = new Cpu();
        entity.setArchitecture("wrongarchitecture");
        new CPUValidator().validateEnums(entity);
    }

    @Test()
    public void validateEnums() {
        Cpu entity = new Cpu();
        entity.setArchitecture("X86_64");
        new CPUValidator().validateEnums(entity);
    }
}
