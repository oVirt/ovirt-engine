package org.ovirt.engine.core.dao;

public interface SystemStatisticsDao {
    Integer getSystemStatisticsValue(String entity);

    Integer getSystemStatisticsValue(String entity, String status);
}
