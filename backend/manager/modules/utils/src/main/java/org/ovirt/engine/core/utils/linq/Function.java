package org.ovirt.engine.core.utils.linq;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 9, 2009 Time: 3:55:53 PM To change this template use File |
 * Settings | File Templates.
 */
public interface Function<IN, OUT> {
    OUT eval(IN in);
}
