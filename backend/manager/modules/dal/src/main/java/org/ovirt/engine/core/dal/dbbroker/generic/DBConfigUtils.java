package org.ovirt.engine.core.dal.dbbroker.generic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.Reloadable;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdcOptionDao;
import org.ovirt.engine.core.utils.ConfigUtilsBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConfigUtils extends ConfigUtilsBase {
    private static final Logger log = LoggerFactory.getLogger(DBConfigUtils.class);

    private static final Map<String, Map<String, Object>> _vdcOptionCache = new HashMap<>();

    /**
     * Refreshes the VDC option cache.
     */
    protected static void refreshVdcOptionCache(DbFacade db) {
        _vdcOptionCache.clear();
        List<VdcOption> list = db.getVdcOptionDao().getAll();
        list.forEach(DBConfigUtils::updateOption);
    }

    /**
     * Initializes a new instance of the DBConfigUtils class.
     */
    public DBConfigUtils() {
        refreshVdcOptionCache(DbFacade.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(ConfigValues name, String version) {
        T returnValue;
        Map<String, Object> values = _vdcOptionCache.get(name.toString());
        if (values != null && values.containsKey(version)) {
            returnValue = (T) values.get(version);
        } else {
            VdcOption option = new VdcOption();
            option.setOptionName(name.toString());
            option.setOptionValue(null);
            // returns default value - version independent
            returnValue = (T) getValue(option);

            // If just requested version is missing, add the default value with the requested version.
            if (values != null) {
                values.put(version, returnValue);
            } else {
                // Couldn't find this value at all, adding to cache.
                Map<String, Object> defaultValues = new HashMap<>();
                defaultValues.put(version, returnValue);
                _vdcOptionCache.put(option.getOptionName(), defaultValues);
                log.debug("Adding new value to configuration cache.");
            }
            log.debug("Didn't find the value of '{}' in DB for version '{}' - using default: '{}'",
                    name, version, returnValue);
        }

        return returnValue;
    }

    /**
     * Refreshes only the reloadable configurations in the VDC option cache.
     */
    public static void refreshReloadableConfigsInVdcOptionCache() {
        List<VdcOption> list = getVdcOptionDao().getAll();
        for (VdcOption option : list) {
            try {
                if (isReloadable(option.getOptionName())) {
                    updateOption(option);
                }
            } catch (NoSuchFieldException e) {
                log.error("Not refreshing field '{}': does not exist in class {}.", option.getOptionName(),
                        ConfigValues.class.getSimpleName());
            }
        }
    }

    private static VdcOptionDao getVdcOptionDao() {
        return DbFacade.getInstance().getVdcOptionDao();
    }

    private static void updateOption(VdcOption option) {
        Map<String, Object> values = _vdcOptionCache.get(option.getOptionName());
        if (values == null) {
            values = new HashMap<>();
            _vdcOptionCache.put(option.getOptionName(), values);
        }
        values.put(option.getVersion(), getValue(option));
    }

    private static boolean isReloadable(String optionName) throws NoSuchFieldException {
        return ConfigValues.class.getField(optionName).isAnnotationPresent(Reloadable.class);
    }
}
