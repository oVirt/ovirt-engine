package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.ovirt.engine.core.common.ExternalVariable;

/**
 * {@link ExternalVariableDao} tests
 */
public class ExternalVariableDaoTest extends BaseDaoTestCase {
    /**
     * Test getting existing variable
     */
    @Test
    public void getExistingVariable() {
        ExternalVariable expected = new ExternalVariable();
        expected.setName("fence-kdump-listener");

        ExternalVariable found = dbFacade.getExternalVariableDao().get(expected.getName());

        assertNotNull(found);
        assertEquals(expected, found);
        assertNotNull(found.getUpdateDate());
    }

    /**
     * Test getting non existent variable
     */
    @Test
    public void getNonExistentVariable() {
        ExternalVariable found = dbFacade.getExternalVariableDao().get("non-existent");

        assertNull(found);
    }

    /**
     * Test create new variable with save
     */
    @Test
    public void insertNewVariable() {
        ExternalVariable newVar = new ExternalVariable();
        newVar.setName("new-var");
        newVar.setValue("123456");
        newVar.setUpdateDate(new Date());

        dbFacade.getExternalVariableDao().save(newVar);

        ExternalVariable found = dbFacade.getExternalVariableDao().get(newVar.getName());

        assertNotNull(found);
        assertEquals(newVar, found);
    }

    /**
     * Test creating new variable with saveOrUpdate
     */
    @Test
    public void saveOrUpdateNewVariable() {
        ExternalVariable newVar = new ExternalVariable();
        newVar.setName("new-var");
        newVar.setValue("123456");
        newVar.setUpdateDate(new Date());

        dbFacade.getExternalVariableDao().saveOrUpdate(newVar);

        ExternalVariable found = dbFacade.getExternalVariableDao().get(newVar.getName());

        assertNotNull(found);
        assertEquals(newVar, found);
    }

    /**
     * Test creating new variable with updated set to {@code null} (db will set updated to now)
     */
    @Test
    public void createNewVariableWithEmptyUpdated() {
        ExternalVariable newVar = new ExternalVariable();
        newVar.setName("new-var");
        newVar.setValue("123456");

        dbFacade.getExternalVariableDao().save(newVar);

        ExternalVariable found = dbFacade.getExternalVariableDao().get(newVar.getName());

        assertNotNull(found);
        assertEquals(newVar.getName(), found.getName());
        assertEquals(newVar.getValue(), found.getValue());
        assertNotNull(found.getUpdateDate());
    }

    /**
     * Test updating existing variable with update
     */
    @Test
    public void updateExistingVariable() {
        String name = "fence-kdump-listener";

        ExternalVariable existing = dbFacade.getExternalVariableDao().get(name);
        existing.setValue("123456");
        existing.setUpdateDate(new Date());

        dbFacade.getExternalVariableDao().update(existing);

        ExternalVariable found = dbFacade.getExternalVariableDao().get(name);

        assertNotNull(found);
        assertEquals(existing, found);
    }

    /**
     * Test updating existing variable with saveOrUpdate
     */
    @Test
    public void saveOrUpdatepdateExistingVariable() {
        String name = "fence-kdump-listener";

        ExternalVariable existing = dbFacade.getExternalVariableDao().get(name);
        existing.setValue("123456");
        existing.setUpdateDate(new Date());

        dbFacade.getExternalVariableDao().saveOrUpdate(existing);

        ExternalVariable found = dbFacade.getExternalVariableDao().get(name);

        assertNotNull(found);
        assertEquals(existing, found);
    }

    /**
     * Test removing existing variable
     */
    @Test
    public void removeExistingVariable() {
        ExternalVariable var = new ExternalVariable();
        var.setName("new-var");
        var.setValue("123456");
        var.setUpdateDate(new Date());

        dbFacade.getExternalVariableDao().save(var);

        ExternalVariable found = dbFacade.getExternalVariableDao().get(var.getName());
        assertNotNull(found);
        assertEquals(var, found);

        dbFacade.getExternalVariableDao().remove(var.getName());

        dbFacade.getExternalVariableDao().get(var.getName());
        ExternalVariable removed = dbFacade.getExternalVariableDao().get(var.getName());
        assertNull(removed);
    }

    /**
     * Test removing existing variable
     */
    @Test
    public void removeNonExistentVariable() {
        dbFacade.getExternalVariableDao().remove("non-existent-var");
    }
}
