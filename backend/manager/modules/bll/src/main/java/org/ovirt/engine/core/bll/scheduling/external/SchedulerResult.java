package org.ovirt.engine.core.bll.scheduling.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerResult {
    private Map<String, List<String>> pluginErrors = null;
    private int resultCode = 0;
    private ArrayList<String> errorMessages = null;

    public Map<String, List<String>> getPluginErrors() {
        return pluginErrors;
    }

    public void addPluginErrors(String pluginName, String errorMessage) {
        if (pluginErrors == null) {
            pluginErrors = new HashMap<>();
        }

        pluginErrors.computeIfAbsent(pluginName, k -> new ArrayList<>());
        pluginErrors.get(pluginName).add(errorMessage);
    }

    public void addError(String errorMessage) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
    }

    public List<String> getErrors() {
        return errorMessages;
    }

    public void setResultCode(int statusCode) {
        resultCode = statusCode;
    }

    public int getResultCode() {
        return resultCode;
    }

}
