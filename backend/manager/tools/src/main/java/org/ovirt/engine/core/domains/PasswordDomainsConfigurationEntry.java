package org.ovirt.engine.core.domains;

import java.util.Map.Entry;

public class PasswordDomainsConfigurationEntry extends DomainsConfigurationEntry {
    private final String LOGGING_PASSWORD = "********";
    public PasswordDomainsConfigurationEntry(String entry, String domainSeperator, String valueSeperator) {
        super(entry, domainSeperator, valueSeperator);
    }

    // This method returns the entry for logging purposes
    @Override
    public String getDomainsLoggingEntry() {
        StringBuilder configurationEntry = new StringBuilder();
        boolean firstEntry = true;

        for (Entry<String, String> currEntry : valuePerDomain.entrySet()) {
            if (!firstEntry) {
                configurationEntry.append(domainSeperator);
            } else {
                firstEntry = false;
            }

            configurationEntry.append(currEntry.getKey());

            if (currEntry.getValue() != null) {
                configurationEntry.append(valueSeperator).append(LOGGING_PASSWORD);
            }

        }
        return configurationEntry.toString();
    }
}
