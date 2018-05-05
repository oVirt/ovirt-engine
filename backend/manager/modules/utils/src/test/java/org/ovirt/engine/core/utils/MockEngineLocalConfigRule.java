package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.common.utils.Pair;

public class MockEngineLocalConfigRule extends TestWatcher {

    private Map<String, String> newValues;

    public MockEngineLocalConfigRule(Pair<String, String>... values) {
        newValues = new HashMap<>();
        for (Pair<String, String> v : values) {
            newValues.put(v.getFirst(), v.getSecond());
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
