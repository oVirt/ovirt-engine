package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;

public class PolicyUnitDaoTest extends BaseDAOTestCase {

    private static final int NUMBER_OF_POLICY_UNITS = 2;
    PolicyUnitDao dao;
    PolicyUnit existingPolicyUnit;
    PolicyUnit dummyPolicyUnit;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getPolicyUnitDao();
        existingPolicyUnit = dao.get(FixturesTool.POLICY_UNIT_MIGRATION);
        createDummyPolicyUnit();
    };

    @Test
    public void testGet() {
        // TODO Auto-generated method stub
        PolicyUnit result = dao.get(FixturesTool.POLICY_UNIT_MIGRATION);
        assertTrue(result.equals(existingPolicyUnit));
    }

    @Test
    public void testGetNegative() {
        PolicyUnit result = dao.get(Guid.newGuid());
        assertNull(result);
    }

    @Test
    public void testGetAll() {
        List<PolicyUnit> result = dao.getAll();

        assertNotNull(result);
        assertEquals(result.size(), NUMBER_OF_POLICY_UNITS);
    }

    private void createDummyPolicyUnit() {
        dummyPolicyUnit = new PolicyUnit();
        dummyPolicyUnit.setId(Guid.newGuid());
        dummyPolicyUnit.setName("Dummy policy unit");
        dummyPolicyUnit.setBalanceImplemeted(true);
    }

}
