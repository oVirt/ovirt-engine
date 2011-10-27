package org.ovirt.engine.core.dal.dbbroker;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Jul 22, 2009 Time: 4:04:26 PM To change this template use File |
 * Settings | File Templates.
 */
public class DbFacadeUtils {
    public static Date fromDate(Timestamp timestamp) {
        if (timestamp == null)
            return null;
        return new java.util.Date(timestamp.getTime());
    }

    public static Object asSingleResult(List<?> list) {
        return list.isEmpty() ? null : list.get(0);
    }
}
