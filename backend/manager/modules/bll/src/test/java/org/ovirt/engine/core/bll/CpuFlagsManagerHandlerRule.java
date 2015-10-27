package org.ovirt.engine.core.bll;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.runner.Description;
import org.ovirt.engine.core.compat.Version;

public class CpuFlagsManagerHandlerRule extends org.junit.rules.TestWatcher {

    private Map<Version, CpuFlagsManagerHandler.CpuFlagsManager> originalManagerDictionary;
    private boolean managerDictionarySet = false;

    @Override
    protected void finished(Description description) {
        super.finished(description);
        if (managerDictionarySet) {
            setManagerDictionary(originalManagerDictionary);
            managerDictionarySet = false;
        }
    }

    private Map<Version, CpuFlagsManagerHandler.CpuFlagsManager> setManagerDictionary(
            Map<Version, CpuFlagsManagerHandler.CpuFlagsManager> managerDictionary) {
        try {
            Field field = CpuFlagsManagerHandler.class.getDeclaredField("_managersDictionary");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<Version, CpuFlagsManagerHandler.CpuFlagsManager> oldManagerDictionary =
                    (Map<Version, CpuFlagsManagerHandler.CpuFlagsManager>) field.get(CpuFlagsManagerHandler.class);
            field.set(CpuFlagsManagerHandler.class, managerDictionary);
            return oldManagerDictionary;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateManagerDictionary(Map<Version, CpuFlagsManagerHandler.CpuFlagsManager> managerDictionary) {
        final Map<Version, CpuFlagsManagerHandler.CpuFlagsManager> oldManagerDictionary =
                setManagerDictionary(managerDictionary);
        if (!managerDictionarySet) {
            managerDictionarySet = true;
            originalManagerDictionary = oldManagerDictionary;
        }
    }
}
