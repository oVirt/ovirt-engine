package org.ovirt.engine.core.utils.linq;

public interface Function<IN, OUT> {
    OUT eval(IN in);
}
