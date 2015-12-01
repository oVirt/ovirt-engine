package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class BatchProcedureCallTest extends BaseDaoTestCase {

    private TagDao dao;

    public BatchProcedureCallTest() {
    }

    @Override
    @Before
    public void setUp() throws Exception {
        dao = dbFacade.getTagDao();
    }

    protected CustomMapSqlParameterSource getParamsSource(Tags tag) {
        CustomMapSqlParameterSource paramsSource = new CustomMapSqlParameterSource(
                dbFacade.getDbEngineDialect());
        paramsSource.addValue("description", tag.getdescription())
                .addValue("tag_id", tag.gettag_id())
                .addValue("tag_name", tag.gettag_name())
                .addValue("parent_id", tag.getparent_id())
                .addValue("readonly", tag.getIsReadonly())
                .addValue("type", tag.gettype());
        return paramsSource;
    }

    @Test
    public void testBatch() {
        List<Tags> tags = dao.getAll();
        for (Tags tag : tags) {
            dao.remove(tag.gettag_id());
        }
        List<Tags> data = new ArrayList<>();
        data.add(new Tags("a", Guid.Empty, true, Guid.newGuid(), "a"));
        data.add(new Tags("b", Guid.Empty, true, Guid.newGuid(), "b"));
        List<MapSqlParameterSource> executions = new ArrayList<>();
        for (Tags tag : data) {
            executions.add(getParamsSource(tag));
        }
        dbFacade.getCallsHandler().executeStoredProcAsBatch("Inserttags",
                executions);
        List<Tags> tagsAfterInsert = dao.getAll();
        assertNotNull(tagsAfterInsert);
        assertEquals(data.size(), tagsAfterInsert.size());
        for (Tags tag : tagsAfterInsert) {
            assertTrue(data.contains(tag));
        }
    }
}
