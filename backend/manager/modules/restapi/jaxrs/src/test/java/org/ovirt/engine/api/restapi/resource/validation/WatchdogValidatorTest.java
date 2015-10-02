package org.ovirt.engine.api.restapi.resource.validation;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Watchdog;

public class WatchdogValidatorTest {
    @Test(expected = WebApplicationException.class)
    public void validateEnumsWrongAction() {
        Watchdog entity = new Watchdog();
        entity.setAction("wrongaction");
        entity.setModel("i6300esb");
        new WatchdogValidator().validateEnums(entity);
    }

    @Test(expected = WebApplicationException.class)
    public void validateEnumsWrongModel() {
        Watchdog entity = new Watchdog();
        entity.setAction("reset");
        entity.setModel("wrongmodel");
        new WatchdogValidator().validateEnums(entity);
    }

    @Test()
    public void validateEnums() {
        Watchdog entity = new Watchdog();
        entity.setAction("reset");
        entity.setModel("i6300esb");
        new WatchdogValidator().validateEnums(entity);
    }
}
