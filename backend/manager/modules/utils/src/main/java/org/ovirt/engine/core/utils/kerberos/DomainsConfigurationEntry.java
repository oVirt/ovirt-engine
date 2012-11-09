package org.ovirt.engine.core.utils.kerberos;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DomainsConfigurationEntry {

    protected Map<String, String> valuePerDomain = new HashMap<String, String>();
    protected String domainSeperator;
    protected String valueSeperator;

    public DomainsConfigurationEntry(String entry, String domainSeperator, String valueSeperator) {
        this.domainSeperator = domainSeperator;
        this.valueSeperator = valueSeperator;
        if (entry != null && !entry.isEmpty()) {
            String[] domainValuePairs = entry.split(domainSeperator);

            for (String domainValuePair : domainValuePairs) {
                if (valueSeperator != null && !valueSeperator.isEmpty()) {
                    String[] pairParts = domainValuePair.split(valueSeperator);
                    valuePerDomain.put(pairParts[0].trim().toLowerCase(), pairParts[1]);
                } else {
                    valuePerDomain.put(domainValuePair.trim(), null);
                }
            }
        }
    }

    public String getDomainsConfigurationEntry() {
        String configurationEntry = "";
        boolean firstEntry = true;

        for ( Entry<String,String> currEntry: valuePerDomain.entrySet() ) {
            if (!firstEntry) {
                configurationEntry += domainSeperator;
            } else {
                firstEntry = false;
            }

            configurationEntry += currEntry.getKey();

            if (currEntry.getValue() != null) {
                configurationEntry += valueSeperator + currEntry.getValue();
            }

        }
        return configurationEntry;
    }

    // This method returns the entry for logging purposes
    public String getDomainsLoggingEntry() {
        return getDomainsConfigurationEntry();
    }

    public void setValueForDomainIfExistent(String domain, String value) {
        if (doesDomainExist(domain)) {
            valuePerDomain.put(domain, value);
        }
    }

    public void setValueForDomain(String domain, String value) {
        valuePerDomain.put(domain, value);
    }


    public void removeValueForDomain(String domain) {
        valuePerDomain.remove(domain);
    }

    public boolean doesDomainExist(String domain) {
        return valuePerDomain.containsKey(domain);
    }

    public boolean isEntryEmpty() {
        return valuePerDomain.isEmpty();
    }

    public String getValueForDomain(String domain) {
        return valuePerDomain.get(domain);
    }

    public Set<Entry<String, String>> getValues() {
        return valuePerDomain.entrySet();
    }

    public Set<String> getDomainNames() {
        return valuePerDomain.keySet();
    }
}
