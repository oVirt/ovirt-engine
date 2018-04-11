package org.ovirt.engine.core.dao;

public interface SystemStatisticsDao extends Dao {
    Integer getSystemStatisticsValue(String entity);

    Integer getSystemStatisticsValue(String entity, String status);
}
