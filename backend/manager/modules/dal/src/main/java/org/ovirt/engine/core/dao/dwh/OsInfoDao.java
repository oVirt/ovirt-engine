package org.ovirt.engine.core.dao.dwh;

import java.util.Map;

public interface OsInfoDao {
    void populateDwhOsInfo(Map<Integer, String> osIdToName);
}
