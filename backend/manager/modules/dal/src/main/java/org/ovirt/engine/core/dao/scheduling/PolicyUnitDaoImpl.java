package org.ovirt.engine.core.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class PolicyUnitDaoImpl extends DefaultGenericDaoDbFacade<PolicyUnit, Guid> implements PolicyUnitDao {

    public PolicyUnitDaoImpl() {
        super("PolicyUnit");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(PolicyUnit entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("name", entity.getName())
                .addValue("is_internal", entity.isInternal())
                .addValue("has_filter", entity.isFilterImplemeted())
                .addValue("has_function", entity.isFunctionImplemeted())
                .addValue("has_balance", entity.isBalanceImplemeted())
                .addValue("custom_properties_regex",
                        SerializationFactory.getSerializer().serialize(entity.getParameterRegExMap()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<PolicyUnit> createEntityRowMapper() {
        return new RowMapper<PolicyUnit>() {

            @Override
            public PolicyUnit mapRow(ResultSet rs, int arg1) throws SQLException {
                PolicyUnit policyUnit = new PolicyUnit();
                policyUnit.setId(getGuid(rs, "id"));
                policyUnit.setName(rs.getString("name"));

                policyUnit.setFilterImplemeted(rs.getBoolean("has_filter"));
                policyUnit.setFunctionImplemeted(rs.getBoolean("has_function"));
                policyUnit.setBalanceImplemeted(rs.getBoolean("has_balance"));
                policyUnit.setParameterRegExMap(SerializationFactory.getDeserializer()
                        .deserializeOrCreateNew(rs.getString("custom_properties_regex"), LinkedHashMap.class));
                return policyUnit;
            }
        };
    }

}
