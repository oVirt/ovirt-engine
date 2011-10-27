package org.ovirt.engine.core.utils.linq;

/**
 * Default mapper only maps in value to key, and the actual value is copied as is
 */
public abstract class DefaultMapper<IN, KEY> implements Mapper<IN, KEY, IN> {
    @Override
    public IN createValue(IN in) {
        return in;
    }
}
