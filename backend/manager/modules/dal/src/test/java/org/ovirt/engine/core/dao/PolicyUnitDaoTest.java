package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;

public class PolicyUnitDaoTest extends BaseDaoTestCase<PolicyUnitDao> {

    private static final int NUMBER_OF_POLICY_UNITS = 2;
    PolicyUnit existingPolicyUnit;
    PolicyUnit dummyPolicyUnit;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingPolicyUnit = dao.get(FixturesTool.POLICY_UNIT_MIGRATION);
        createDummyPolicyUnit();
    }

    @Test
    public void testGet() {
        PolicyUnit result = dao.get(FixturesTool.POLICY_UNIT_MIGRATION);
        assertEquals(result, existingPolicyUnit);
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
        assertEquals(NUMBER_OF_POLICY_UNITS, result.size());
    }

    private void createDummyPolicyUnit() {
        dummyPolicyUnit = new PolicyUnit();
        dummyPolicyUnit.setId(Guid.newGuid());
        dummyPolicyUnit.setName("Dummy policy unit");
        dummyPolicyUnit.setDescription("Description");
        dummyPolicyUnit.setPolicyUnitType(PolicyUnitType.FILTER);
        dummyPolicyUnit.setParameterRegExMap(new LinkedHashMap<>());
    }

    @Test
    public void testSave() {
        dao.save(dummyPolicyUnit);
        PolicyUnit result = dao.get(dummyPolicyUnit.getId());
        assertEquals(result, dummyPolicyUnit);
    }

    @Test
    public void testUpdate() {
        PolicyUnit policyUnitToUpdate = dao.get(FixturesTool.POLICY_UNIT_MIGRATION);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("A", "B");
        policyUnitToUpdate.setParameterRegExMap(map);
        policyUnitToUpdate.setDescription("dummy description");
        dao.update(policyUnitToUpdate);

        PolicyUnit result = dao.get(policyUnitToUpdate.getId());
        assertEquals(result, policyUnitToUpdate);
    }
}
