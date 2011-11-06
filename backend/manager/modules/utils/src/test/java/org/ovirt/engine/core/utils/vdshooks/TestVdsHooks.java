package org.ovirt.engine.core.utils.vdshooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.ValueObjectPair;
import static org.junit.Assert.*;

public class TestVdsHooks {

    private Set<String> events;
    private Map<String, Map<String, String>> scriptsForBeforeVmHibernate;
    private Map<String, Map<String, String>> scriptsForAfterVmMigrateDestination;
    private Map<String, Map<String, String>> scriptsForAfterVmCont;
    private Map<String, Map<String, Map<String, String>>> eventsToScriptsMap;

    private void init() {
        // Prepare a map from events to scripts information
        events =
                new HashSet<String>(Arrays.asList("before_vm_hibernate",
                        "after_vm_migrate_destination",
                        "after_vm_cont"));
        scriptsForBeforeVmHibernate = new HashMap<String, Map<String, String>>();
        scriptsForAfterVmMigrateDestination = new HashMap<String, Map<String, String>>();
        scriptsForAfterVmCont = new HashMap<String, Map<String, String>>();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("md5", "5d9c4609cd936e80bac8c9ef7b27ea73");
        scriptsForBeforeVmHibernate.put("myscript.sh", properties);
        properties = new HashMap<String, String>();
        properties.put("md5", "f388923356e84c2b4149572a44fde2b4");
        scriptsForBeforeVmHibernate.put("01_log", properties);
        properties = new HashMap<String, String>();
        properties.put("md5", "677da3bdd8fbd16d4b8917a9fe0f6f89");
        scriptsForAfterVmMigrateDestination.put("myscript.sh", properties);
        properties = new HashMap<String, String>();
        properties.put("md5", "f388923356e84c2b4149572a44fde2b4");

        scriptsForAfterVmCont.put("01_log", properties);
        /*
         * scriptsForBeforeVmHibernate = new HashSet<String>(Arrays.asList("myscript.sh","01_log"));
         * scriptsForAfterVmMigrateDestination = new HashSet<String>(Arrays.asList("myscript.sh"));
         * scriptsForAfterVmCont = new HashSet<String>(Arrays.asList("01_log"));
         */
        eventsToScriptsMap = new HashMap<String, Map<String, Map<String, String>>>();
        eventsToScriptsMap.put("before_vm_hibernate", scriptsForBeforeVmHibernate);
        eventsToScriptsMap.put("after_vm_migrate_destination", scriptsForAfterVmMigrateDestination);
        eventsToScriptsMap.put("after_vm_cont", scriptsForAfterVmCont);
    }

    @Test
    public void testVdsHooks() {
        init();
        String hooksStr =
                "{before_vm_hibernate={myscript.sh={md5=5d9c4609cd936e80bac8c9ef7b27ea73}, 01_log={md5=f388923356e84c2b4149572a44fde2b4}}, after_vm_migrate_destination={myscript.sh={md5=677da3bdd8fbd16d4b8917a9fe0f6f89}}, after_vm_cont={01_log={md5=f388923356e84c2b4149572a44fde2b4}}}";
        // Prepare data structures for testing the internal data of the value objects map

        ValueObjectMap result = VdsHooksParser.parseHooks(hooksStr);
        ArrayList<ValueObjectPair> eventsValuePairs = result.getValuePairs();
        assertEquals(3, eventsValuePairs.size());

        for (ValueObjectPair eventsValuePair : eventsValuePairs) {
            String event = (String) eventsValuePair.getKey();
            ValueObjectMap scriptsObjectValuePairs = (ValueObjectMap) eventsValuePair.getValue();
            assertTrue(events.contains(event));
            testEvent(event, scriptsObjectValuePairs);
        }
    }

    private void testEvent(String event, ValueObjectMap scriptsObjectValueMap) {
        Map<String, Map<String, String>> scriptsMap = eventsToScriptsMap.get(event);
        for (ValueObjectPair scriptValueObjectPair : scriptsObjectValueMap.getValuePairs()) {
            String key = (String) scriptValueObjectPair.getKey();
            assertTrue(scriptsMap.keySet().contains(key));
            ValueObjectMap properties = (ValueObjectMap) scriptValueObjectPair.getValue();
            Map<String, String> expectedProperties = scriptsMap.get(key);
            testProperties(properties, expectedProperties);
        }

    }

    private void testProperties(ValueObjectMap properties, Map<String, String> expectedProperties) {
        ArrayList<ValueObjectPair> valuePairs = properties.getValuePairs();
        for (ValueObjectPair pair : valuePairs) {
            String key = (String) pair.getKey();
            String value = (String) pair.getValue();
            String expectedValue = expectedProperties.get(key);
            assertEquals(expectedValue, value);
        }
        // TODO Auto-generated method stub

    }

}
