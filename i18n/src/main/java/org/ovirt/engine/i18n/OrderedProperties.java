package org.ovirt.engine.i18n;

import java.util.*;

public class OrderedProperties extends Properties {
    private static final long serialVersionUID = 4696173303087585351L;

    /**
     * Return a natural ordered set of the Properties' keys.
     */
    @Override
    public Set<Object> keySet() {
        TreeSet<Object> ret = new TreeSet<>(
            Comparator.comparing(Object::toString)
        );
        ret.addAll(super.keySet());
        return ret;
    }

    /**
     * Return an enumeration over a natural ordered set of the Properties' keys.
     */
    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(keySet());
    }

    /**
     * Return a natural ordered by key set of the Properties' entries.
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        TreeSet<Map.Entry<Object, Object>> ret = new TreeSet<>(
            Comparator.comparing(l -> l.getKey().toString())
        );
        ret.addAll(super.entrySet());
        return ret;
    }

}
