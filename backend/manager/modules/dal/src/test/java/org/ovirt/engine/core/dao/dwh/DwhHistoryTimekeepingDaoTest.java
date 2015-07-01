package org.ovirt.engine.core.dao.dwh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeepingVariable;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

/**
 * {@link DwhHistoryTimekeepingDao} tests
 */
public class DwhHistoryTimekeepingDaoTest extends BaseDaoTestCase {
    /**
     * Tests reading existing variables
     */
    @Test
    public void getAllExistingVariables() {
        for (DwhHistoryTimekeepingVariable var: DwhHistoryTimekeepingVariable.values()) {
            if (var != DwhHistoryTimekeepingVariable.UNDEFINED) {
                assertNotNull(dbFacade.getDwhHistoryTimekeepingDao().get(var));
            }
        }
    }

    /**
     * Tests reading nonexistent variable
     */
    @Test
    public void getNonexistentVariable() {
        assertNull(dbFacade.getDwhHistoryTimekeepingDao().get(DwhHistoryTimekeepingVariable.UNDEFINED));
    }

    /**
     * Tests saving variable
     */
    @Test
    public void testSave() {
        DwhHistoryTimekeeping var = new DwhHistoryTimekeeping();
        var.setVariable(DwhHistoryTimekeepingVariable.HEART_BEAT);
        var.setValue(null);
        var.setDateTime(new Date());

        DwhHistoryTimekeepingDao dao = dbFacade.getDwhHistoryTimekeepingDao();

        dao.save(var);

        DwhHistoryTimekeeping found = dao.get(var.getVariable());

        assertNotNull(found);
        assertEquals(var.getVariable(), found.getVariable());
        assertEquals(var.getValue(), found.getValue());
        assertEquals(var.getDateTime(), found.getDateTime());
    }
}
