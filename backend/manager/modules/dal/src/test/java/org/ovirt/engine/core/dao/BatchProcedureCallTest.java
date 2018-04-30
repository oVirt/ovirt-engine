package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class BatchProcedureCallTest extends BaseDaoTestCase<TagDao> {
    @Inject
    private DbEngineDialect dbEngineDialect;

    @Inject
    private SimpleJdbcCallsHandler jdbcCallsHandler;

    protected CustomMapSqlParameterSource getParamsSource(Tags tag) {
        CustomMapSqlParameterSource paramsSource = new CustomMapSqlParameterSource(dbEngineDialect);
        paramsSource.addValue("description", tag.getDescription())
                .addValue("tag_id", tag.getTagId())
                .addValue("tag_name", tag.getTagName())
                .addValue("parent_id", tag.getParentId())
                .addValue("readonly", tag.getIsReadonly())
                .addValue("type", tag.getType());
        return paramsSource;
    }

    @Test
    public void testBatch() {
        List<Tags> tags = dao.getAll();
        for (Tags tag : tags) {
            dao.remove(tag.getTagId());
        }
        List<Tags> data = new ArrayList<>();
        data.add(new Tags("a", Guid.Empty, true, Guid.newGuid(), "a"));
        data.add(new Tags("b", Guid.Empty, true, Guid.newGuid(), "b"));
        List<MapSqlParameterSource> executions = data.stream().map(this::getParamsSource).collect(Collectors.toList());
        jdbcCallsHandler.executeStoredProcAsBatch("Inserttags", executions);
        List<Tags> tagsAfterInsert = dao.getAll();
        assertNotNull(tagsAfterInsert);
        assertEquals(data.size(), tagsAfterInsert.size());
        assertTrue(data.containsAll(tagsAfterInsert));
    }
}
