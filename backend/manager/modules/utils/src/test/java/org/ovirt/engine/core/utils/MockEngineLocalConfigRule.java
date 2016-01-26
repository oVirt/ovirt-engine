package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class MockEngineLocalConfigRule extends TestWatcher {

    public static class KeyValue {
        String key;
        String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private Map<String, String> newValues;

    public MockEngineLocalConfigRule(KeyValue... values) {
        newValues = new HashMap<>();
        for (KeyValue v : values) {
            newValues.put(v.key, v.value);
        }
    }

    @Override
    public void starting(Description description) {
        EngineLocalConfig.getInstance(newValues);
    }

    @Override
    public void finished(Description description) {
        EngineLocalConfig.clearInstance();
    }

}
