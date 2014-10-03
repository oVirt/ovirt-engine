package org.ovirt.engine.core.config;

import java.util.Arrays;
import java.util.List;

/**
 * The <code>OptionKey</code> enum holds the types of options in the EngineConfig tool. Each OptionKey holds a list of
 * strings that represent it.
 */
public enum OptionKey {
        OPTION_CONFIG(Arrays.asList(new String[] { "-c", "--config" })),
        OPTION_PROPERTIES(Arrays.asList(new String[] { "-p", "--properties" })),
        OPTION_VERSION(Arrays.asList(new String[] { "--cver" })),
        OPTION_USER(Arrays.asList(new String[] { "-u", "--user" })),
        OPTION_ADMINPASSFILE(Arrays.asList(new String[] { "--admin-pass-file" })),
        OPTION_ONLY_RELOADABLE(Arrays.asList(new String[] { "-o", "--only-reloadable" })),
        OPTION_LOG_FILE(Arrays.asList(new String[] { "--log-file" })),
        OPTION_LOG_LEVEL(Arrays.asList(new String[] { "--log-level" }));

    private List<String> optionalStrings;

    private OptionKey(List<String> optionalStrings) {
        this.optionalStrings = optionalStrings;
    }

    public List<String> getOptionalStrings() {
        return optionalStrings;
    }
}
