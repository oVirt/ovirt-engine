package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.WatchDog;

public class WatchdogValidatorTest {
    @Test(expected = WebApplicationException.class)
    public void validateEnumsWrongAction() {
        WatchDog entity = new WatchDog();
        entity.setAction("wrongaction");
        entity.setModel("i6300esb");
        new WatchdogValidator().validateEnums(entity);
    }

    @Test(expected = WebApplicationException.class)
    public void validateEnumsWrongModel() {
        WatchDog entity = new WatchDog();
        entity.setAction("reset");
        entity.setModel("wrongmodel");
        new WatchdogValidator().validateEnums(entity);
    }

    @Test()
    public void validateEnums() {
        WatchDog entity = new WatchDog();
        entity.setAction("reset");
        entity.setModel("i6300esb");
        new WatchdogValidator().validateEnums(entity);
    }
}
