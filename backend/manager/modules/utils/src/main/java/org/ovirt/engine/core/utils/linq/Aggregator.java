package org.ovirt.engine.core.utils.linq;

public interface Aggregator<VALUE> {
    VALUE process(VALUE aggregate, VALUE value);
}
