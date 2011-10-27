package org.ovirt.engine.api.restapi.test.mappers.impl;

import org.ovirt.engine.api.restapi.test.mappers.api.IBar;

public class BarImpl implements IBar {

    private String s;
    private String other;

    public BarImpl() {
    }

    public BarImpl(String s) {
        this.s = s;
    }

    public BarImpl(String s, String other) {
        this(s);
        this.other = other;
    }

    @Override
    public String get() {
        return s;
    }

    @Override
    public void set(String s) {
        this.s = s;
    }

    public String other() {
        return other;
    }
}
