package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ValueObjectMap extends ValueObject implements Serializable {
    private static final long serialVersionUID = -8970215546874151379L;

    private ArrayList<ValueObjectPair> valuePairs = new ArrayList<ValueObjectPair>();

    public ValueObjectMap() {
    }

    public ValueObjectMap(Map map, boolean mapOfMaps) {
        valuePairs = new ArrayList<ValueObjectPair>(map.keySet().size());
        int i = 0;
        // if the value is also a map construct a ValueObjectMap from the value
        // as well.
        if (mapOfMaps) {
            for (Object e : map.entrySet()) {
                Map.Entry<Object, Map> entryMap = (Map.Entry<Object, Map>) e;
                Map innerMap = entryMap.getValue();
                boolean innerMapIsMapOfMaps = false;
                // If map of maps, it is possible the inner map is also a map of maps
                // So the inner ValueObjectMap should be constructed accordingly
                // To determine if the inner map is also a map, the set of entries of the map is retrieved.
                // If there is at least a single entry, it will be checked if this entry has a value which is a map
                // itself
                Set entries = innerMap.entrySet();
                Iterator entriesIterator = entries.iterator();
                if (entriesIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entriesIterator.next();
                    if (entry.getValue() instanceof Map) {
                        innerMapIsMapOfMaps = true;
                    }
                }

                valuePairs.add(new ValueObjectPair(entryMap.getKey(), new ValueObjectMap(innerMap, innerMapIsMapOfMaps)));
                ++i;
            }
        } else {
            for (Object key : map.keySet()) {
                valuePairs.add(new ValueObjectPair(key, map.get(key)));
                ++i;
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + valuePairs.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueObjectMap other = (ValueObjectMap) obj;
        if (!valuePairs.equals(other.valuePairs))
            return false;
        return true;
    }

    public ArrayList<ValueObjectPair> getValuePairs() {
        return valuePairs;
    }

    public void setValuePairs(ArrayList<ValueObjectPair> valuePairs) {
        if (valuePairs != null) {
            this.valuePairs = valuePairs;
        } else {
            this.valuePairs = new ArrayList<ValueObjectPair>();
        }
    }

    @Override
    public Map asMap() {
        HashMap hashMap = new HashMap();
        for (ValueObjectPair valuePair : valuePairs) {
            hashMap.put(valuePair.getKey(), valuePair.getValue());
        }
        return hashMap;
    }
}
