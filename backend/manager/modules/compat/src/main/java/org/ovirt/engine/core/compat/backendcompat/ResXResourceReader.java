package org.ovirt.engine.core.compat.backendcompat;

import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.compat.ApplicationException;

public class ResXResourceReader extends LinkedList<Entry<String, Object>> {

    private static final long serialVersionUID = -1668354571890766752L;
    private Log log = LogFactory.getLog(ResXResourceReader.class);

    // Although this is ResXReader the assumption is that the
    // string is a path to a properties file.
    public ResXResourceReader(String appErrorsFileName) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(appErrorsFileName);
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String value = bundle.getString(key);
                Entry<String, Object> entry = new AbstractMap.SimpleEntry<String, Object>(key, value);
                this.addLast(entry);
            }
        } catch (MissingResourceException e) {
            log.error("Could not load the resources for " + appErrorsFileName);
            throw new ApplicationException(e);
        }
    }

}
