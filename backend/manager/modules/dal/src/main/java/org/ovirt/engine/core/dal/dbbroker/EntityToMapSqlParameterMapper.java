package org.ovirt.engine.core.dal.dbbroker;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public interface EntityToMapSqlParameterMapper<T extends BusinessEntity<?>> {
    public MapSqlParameterSource map(T entity);
}
