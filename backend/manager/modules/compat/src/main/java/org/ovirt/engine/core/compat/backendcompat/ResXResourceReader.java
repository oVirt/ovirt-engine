package org.ovirt.engine.core.compat.backendcompat;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.LinkedList;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.ovirt.engine.core.compat.CompatException;
import org.ovirt.engine.core.compat.DictionaryEntry;

public class ResXResourceReader extends LinkedList<DictionaryEntry> {
    private Log log = LogFactory.getLog(ResXResourceReader.class);

    // Although this is ResXReader the assumption is that the
    // string is a path to a properties file.
    public ResXResourceReader(String appErrorsFileName) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(appErrorsFileName);
            Enumeration keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = bundle.getString(key);
                DictionaryEntry entry = new DictionaryEntry(key, value);
                this.addLast(entry);
            }
        } catch (MissingResourceException e) {
            log.error("Could not load the resources for " + appErrorsFileName);
            throw new CompatException(e);
        }
    }

}
