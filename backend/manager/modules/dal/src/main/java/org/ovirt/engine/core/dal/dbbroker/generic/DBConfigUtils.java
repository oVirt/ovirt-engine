package org.ovirt.engine.core.dal.dbbroker.generic;

import static org.ovirt.engine.core.common.config.OptionBehaviour.ValueDependent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
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
        List<VdcOption> list = moveDependentToEnd(getVdcOptionDao().getAll());
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
        if (valueExists(name, version)) {
            return values.get(version);
        }
        throw new IllegalArgumentException(name.toString() + " has no value for version: " + version);
    }

    @Override
    public boolean valueExists(ConfigValues configValue, String version) {
        Map<String, Object> values = getValuesForAllVersions(configValue);
        return values != null && values.containsKey(version);
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

    private List<VdcOption> moveDependentToEnd(List<VdcOption> list) {
        Predicate<VdcOption> isDependent =
            o -> {
                EnumValue parsed = parseEnumValue(o.getOptionName());
                if (parsed != null) {
                    OptionBehaviourAttribute behaviour = parsed.getOptionBehaviour();
                    return behaviour != null && behaviour.behaviour() == ValueDependent;
                }
                return false;
            };
        List<VdcOption> optionsList = list.stream().filter(isDependent.negate()).collect(Collectors.toList());
        optionsList.addAll(list.stream().filter(isDependent).collect(Collectors.toList()));
        return optionsList;
    }
}
