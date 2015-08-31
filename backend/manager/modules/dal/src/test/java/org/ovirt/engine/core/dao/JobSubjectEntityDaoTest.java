package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class JobSubjectEntityDaoTest extends BaseDaoTestCase {

    private static final Guid EXISTING_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5111");
    private static final Guid EXISTING_ENTITY_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");

    private static final int TOTAL_JOBS_SUBJECT_ENTITIES = 1;

    private JobSubjectEntityDao dao;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getJobSubjectEntityDao();
    }

    @Test
    public void getJobSubjectEntityByJobId() {
        Map<Guid, VdcObjectType> entitybeforeSave = dao.getJobSubjectEntityByJobId(EXISTING_JOB_ID);
        assertEquals("Compare of existing to expected amount of entities",
                TOTAL_JOBS_SUBJECT_ENTITIES,
                entitybeforeSave.size());
    }

    @Test
    public void saveJobSubjectEntity() {
        Map<Guid, VdcObjectType> entitybeforeSave = dao.getJobSubjectEntityByJobId(EXISTING_JOB_ID);
        assertTrue(!entitybeforeSave.isEmpty());
        dao.save(EXISTING_JOB_ID, Guid.newGuid(), VdcObjectType.VmPool);
        Map<Guid, VdcObjectType> entityAfterSave = dao.getJobSubjectEntityByJobId(EXISTING_JOB_ID);
        assertEquals("Job subject entities before and after adding new entity",
                entitybeforeSave.size() + 1,
                entityAfterSave.size());
    }

    @Test
    public void getJobIdByEntityIdAndEntityType() {
        List<Guid> jobIdByEntityIdAndEntityType =
                dao.getJobIdByEntityId(EXISTING_ENTITY_ID);
        assertTrue("Verify job subject entities exist for a given job",
                jobIdByEntityIdAndEntityType.contains(EXISTING_JOB_ID));
    }
}
