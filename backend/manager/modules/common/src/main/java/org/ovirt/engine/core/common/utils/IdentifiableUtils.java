package org.ovirt.engine.core.common.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Identifiable;

public abstract class IdentifiableUtils {
    public static Set<Integer> getValues(Collection<? extends Identifiable> collection) {
        Set<Integer> values = new HashSet<>();
        for (Identifiable identifiable : collection) {
            values.add(identifiable.getValue());
        }

        return values;
    }
}
