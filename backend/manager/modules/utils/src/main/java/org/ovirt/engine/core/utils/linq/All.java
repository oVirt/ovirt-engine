package org.ovirt.engine.core.utils.linq;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Sep 16, 2009 Time: 4:23:06 PM To change this template use File |
 * Settings | File Templates.
 */
public class All<T> implements Predicate<T> {
    @Override
    public boolean eval(T t) {
        return true;
    }
}
