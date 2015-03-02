package org.ovirt.engine.core.dao;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.utils.ResourceUtils;

public class DaoFactory {

    private static final String ENGINE_DAOS_CONFIGURATION_FILE = "engine-daos.properties";

    private static ConcurrentMap<Class<? extends DAO>, DAO> daos = new ConcurrentHashMap<Class<? extends DAO>, DAO>();

    public static <T extends DAO> T get(Class<T> daoType) {
        if (!daos.containsKey(daoType)) {
            daos.putIfAbsent(daoType, createDAO(daoType));
        }
        return daoType.cast(daos.get(daoType));
    }

    private static <T extends DAO> T createDAO(Class<T> daoType) {
        try {
            return newInstance(daoType,
                    ResourceUtils.loadProperties(DaoFactory.class, ENGINE_DAOS_CONFIGURATION_FILE));
        } catch (Exception e) {
            throw new DaoFactoryException(daoType, ENGINE_DAOS_CONFIGURATION_FILE, e);
        }
    }

    private static <T extends DAO> T newInstance(Class<T> daoType, Properties props) throws Exception {
        String className = props.getProperty(daoType.getSimpleName());
        return daoType.cast(Class.forName(className).newInstance());
    }
}
