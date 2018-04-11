package org.ovirt.engine.core.dao.dwh;

import java.util.Map;

import org.ovirt.engine.core.dao.Dao;

public interface OsInfoDao extends Dao {
    void populateDwhOsInfo(Map<Integer, String> osIdToName);
}
