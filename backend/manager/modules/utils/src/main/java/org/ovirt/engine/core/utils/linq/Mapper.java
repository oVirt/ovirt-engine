package org.ovirt.engine.core.utils.linq;

public interface Mapper<IN, KEY, VALUE> {
    KEY createKey(IN in);

    VALUE createValue(IN in);
}
