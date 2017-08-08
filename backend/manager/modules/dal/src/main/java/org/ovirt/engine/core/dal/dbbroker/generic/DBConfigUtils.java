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
    public void refresh() {
        _vdcOptionCache.clear();
        List<VdcOption> list = getVdcOptionDao().getAll();
        for (VdcOption option : list) {
            try {
                if (!_vdcOptionCache.containsKey(option.getOptionName()) ||
                        !_vdcOptionCache.get(option.getOptionName()).containsKey(option.getVersion()) ||
                        isReloadable(option.getOptionName())) {
                    updateOption(option);
                }
            } catch (NoSuchFieldException e) {
                log.error("Not refreshing field '{}': does not exist in class {}.", option.getOptionName(),
                        ConfigValues.class.getSimpleName());
            }
        }
    }

    /**
     * Initializes a new instance of the DBConfigUtils class.
     */
    public DBConfigUtils() {
        refresh();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getValuesForAllVersions(ConfigValues configValue) {
        return (Map<String, T>) _vdcOptionCache.get(configValue.toString());
    }

    @Override
    public <T> T getValue(ConfigValues name, String version) {
        Map<String, T> values = getValuesForAllVersions(name);
        if (values != null && values.containsKey(version)) {
            return values.get(version);
        }
        throw new IllegalArgumentException(name.toString() + " has no value for version: " + version);
    }

    private static VdcOptionDao getVdcOptionDao() {
        return DbFacade.getInstance().getVdcOptionDao();
    }

    private void updateOption(VdcOption option) {
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
