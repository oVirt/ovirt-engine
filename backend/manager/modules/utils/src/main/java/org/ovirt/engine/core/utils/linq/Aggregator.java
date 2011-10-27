package org.ovirt.engine.core.utils.linq;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Oct 8, 2009 Time: 11:40:27 AM To change this template use File |
 * Settings | File Templates.
 */
public interface Aggregator<VALUE> {
    VALUE process(VALUE aggregate, VALUE value);
}
