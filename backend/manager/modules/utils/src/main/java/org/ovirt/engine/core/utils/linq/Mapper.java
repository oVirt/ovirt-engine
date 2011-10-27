package org.ovirt.engine.core.utils.linq;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 9, 2009 Time: 4:21:39 PM To change this template use File |
 * Settings | File Templates.
 */
public interface Mapper<IN, KEY, VALUE> {
    KEY createKey(IN in);

    VALUE createValue(IN in);
}
