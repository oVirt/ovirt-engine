package org.ovirt.engine.core.dal.dbbroker;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public interface MapSqlParameterMapper<T> {
    public MapSqlParameterSource map(T entity);
}
