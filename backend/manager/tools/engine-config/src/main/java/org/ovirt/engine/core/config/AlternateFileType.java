package org.ovirt.engine.core.config;

import java.util.Arrays;
import java.util.List;

/**
 * The <code>AlternateFileType</code> enum holds the optional types of alternate files in the EngineConfig tool. Each
 * AlternateFileType holds a list of strings that represent it.
 */
public enum AlternateFileType {
        OPTION_CONFIG(Arrays.asList(new String[] { "-c", "--config" })),
        OPTION_PROPERTIES(Arrays.asList(new String[] { "-p", "--properties" }));

    private List<String> optionalStrings;

    private AlternateFileType(List<String> optionalStrings) {
        this.optionalStrings = optionalStrings;
    }

    public List<String> getOptionalStrings() {
        return optionalStrings;
    }
}
