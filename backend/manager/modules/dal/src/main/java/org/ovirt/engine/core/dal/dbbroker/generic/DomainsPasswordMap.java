package org.ovirt.engine.core.dal.dbbroker.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

public class DomainsPasswordMap implements Map<String, String> {

    private final Map<String, String> map;

    /**
     * This structure is a map of domains to decrypted password. It initializes from a comma separated list of
     * domain:password values. e.g. <code>REDHAT.COM:{encrypted-password}, JBOSS.COM:{encrypted-password}</code>
     * @param csvOfDomainToPassword
     *            - comma delimited list of domain:password values
     * @param storeUrl
     * @param storePass
     * @param certAlias
     */
    public DomainsPasswordMap(String csvOfDomainToPassword) {
        if (!csvOfDomainToPassword.isEmpty()) {
            String[] domainPasswordPairs = csvOfDomainToPassword.split(",");
            map = new HashMap<String, String>(domainPasswordPairs.length);
            for (String domainPasswordPair : domainPasswordPairs) {
                String[] parts = domainPasswordPair.split(":");
                String domain = parts[0].trim().toLowerCase();
                String password = parts[1].trim();
                try {
                    password = EngineEncryptionUtils.decrypt(password);
                } catch (Exception e) {
                    // failed decrypting the password - password may not be encrypted in first place or clear text
                    // already
                }
                map.put(domain, password);
            }
        } else {
            map = Collections.emptyMap();
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return map.get(key);
    }

    @Override
    public String put(String key, String value) {
        return map.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<String> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return map.entrySet();
    }

}
