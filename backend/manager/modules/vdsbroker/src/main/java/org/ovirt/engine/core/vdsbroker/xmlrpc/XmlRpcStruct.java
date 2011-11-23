package org.ovirt.engine.core.vdsbroker.xmlrpc;

import java.util.*;

public class XmlRpcStruct {

    private Map<String, Object> innerMap;

    public XmlRpcStruct(Map<String, Object> innerMap) {
        this.innerMap = innerMap;
    }

    public XmlRpcStruct() {
        this.innerMap = new HashMap<String, Object>();
    }

    public Object getItem(String key) {
        return innerMap.get(key);
    }

    public void add(String key, Map<String, String> map) {
        innerMap.put(key, map);
    }

    public void add(String key, List<String> map) {
        innerMap.put(key, map);
    }

    public void add(String key, XmlRpcStruct map) {
        innerMap.put(key, map.getInnerMap());
    }

    public void add(String key, String value) {
        innerMap.put(key, value);

    }

    public boolean containsKey(String name) {
        return innerMap.containsKey(name);
    }

    public boolean contains(String diskTotal) {
        return innerMap.containsKey(diskTotal);
    }

    public Set<String> getKeys() {
        return innerMap.keySet();
    }

    public void add(String key, Map[] drives) {
        innerMap.put(key, drives);
    }

    public int getCount() {
        if (innerMap != null) {
            return innerMap.size();
        } else {
            return 0;
        }
    }

    public void add(String sysprepInf, byte[] binarySysPrep) {
        innerMap.put(sysprepInf, binarySysPrep);

    }

    public void add(String key, int value) {
        innerMap.put(key, value);

    }

    public Set<Map.Entry<String, Object>> getEntries() {
        return innerMap.entrySet();
    }

    public Map<String, Object> getInnerMap() {
        return innerMap;
    }
}
