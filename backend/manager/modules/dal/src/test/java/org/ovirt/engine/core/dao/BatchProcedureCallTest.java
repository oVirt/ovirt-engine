package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class BatchProcedureCallTest extends BaseDAOTestCase {

    private TagDAO dao;

    public BatchProcedureCallTest() {
    }

    @Override
    @Before
    public void setUp() throws Exception {
        dao = dbFacade.getTagDao();
    }

    protected CustomMapSqlParameterSource getParamsSource(tags tag) {
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
        List<tags> tags = dao.getAll();
        for (tags tag : tags) {
            dao.remove(tag.gettag_id());
        }
        List<tags> data = new ArrayList<>();
        data.add(new tags("a", Guid.Empty, true, Guid.NewGuid(), "a"));
        data.add(new tags("b", Guid.Empty, true, Guid.NewGuid(), "b"));
        List<MapSqlParameterSource> executions = new ArrayList<MapSqlParameterSource>();
        for (tags tag : data) {
            executions.add(getParamsSource(tag));
        }
        dbFacade.getCallsHandler().executeStoredProcAsBatch("Inserttags",
                executions);
        List<tags> tagsAfterInsert = dao.getAll();
        assertNotNull(tagsAfterInsert);
        assertEquals(data.size(), tagsAfterInsert.size());
        for (tags tag : tagsAfterInsert) {
            assertTrue(data.contains(tag));
        }
    }
}
