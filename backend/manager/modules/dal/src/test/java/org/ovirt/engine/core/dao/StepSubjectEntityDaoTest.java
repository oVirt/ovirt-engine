package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.compat.Guid;

public class StepSubjectEntityDaoTest extends BaseDaoTestCase {
    private StepSubjectEntityDao dao;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getStepSubjectEntityDao();
    }

    @Test
    public void saveStepSubjectEntities() {
        VdcObjectType type = VdcObjectType.VmPool;
        Guid entityId = Guid.newGuid();
        StepSubjectEntity stepSubjectEntity = new StepSubjectEntity(FixturesTool.STEP_ID, type, entityId, 50);
        Guid entityId2 = Guid.newGuid();
        StepSubjectEntity stepSubjectEntity2 = new StepSubjectEntity(FixturesTool.STEP_ID, type, entityId2, 50);
        dao.saveAll(Arrays.asList(stepSubjectEntity, stepSubjectEntity2));
        List<StepSubjectEntity> entities = dao.getStepSubjectEntitiesByStepId(FixturesTool.STEP_ID);
        assertEquals("StepSubjectEntity list not in the expected size", 4, entities.size());
        assertSubjectEntityPresence(stepSubjectEntity, entities, true);
        assertSubjectEntityPresence(stepSubjectEntity2, entities, true);
    }

    @Test
    public void getStepSubjectEntityByStepId() {
        List<StepSubjectEntity> entities = dao.getStepSubjectEntitiesByStepId(FixturesTool.STEP_ID);
        assertEquals("StepSubjectEntity list not in the expected size", 2, entities.size());
        StepSubjectEntity stepSubjectEntity = new StepSubjectEntity(FixturesTool.STEP_ID,
                VdcObjectType.Storage, FixturesTool.IMAGE_GROUP_ID, 50);
        assertSubjectEntityPresence(stepSubjectEntity, entities, true);
    }

    private void assertSubjectEntityPresence(StepSubjectEntity stepSubjectEntity, List<StepSubjectEntity> entities,
                                             boolean shouldBePresent) {
        boolean isPresent = entities.stream().anyMatch(p -> p.equals(stepSubjectEntity) &&
                p.getStepEntityWeight().equals(stepSubjectEntity.getStepEntityWeight()));

        assertEquals("StepSubjectEntity was " + (shouldBePresent ? "not " : "") +
                "found in the entities list although wasn't expected to", shouldBePresent, isPresent);
    }

    @Test
    public void remove() {
        List<StepSubjectEntity> entities = dao.getStepSubjectEntitiesByStepId(FixturesTool.STEP_ID);
        assertEquals("StepSubjectEntity list not in the expected size", 2, entities.size());
        assertNotEquals("StepSubjectEntity list elements should be different", entities.get(0), entities.get(1));
        StepSubjectEntity toRemove = entities.remove(0);
        dao.remove(toRemove.getEntityId(), toRemove.getStepId());
        entities = dao.getStepSubjectEntitiesByStepId(FixturesTool.STEP_ID);
        assertEquals("StepSubjectEntity list not in the expected size", 1, entities.size());
        assertSubjectEntityPresence(toRemove, entities, false);
    }
}
