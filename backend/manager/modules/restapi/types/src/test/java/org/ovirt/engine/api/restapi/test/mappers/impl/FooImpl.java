package org.ovirt.engine.api.restapi.test.mappers.impl;

import org.ovirt.engine.api.restapi.test.mappers.api.IFoo;

public class FooImpl implements IFoo {

    private String s;
    private String other;

    public FooImpl() {
    }

    public FooImpl(String s) {
        this.s = s;
    }

    public FooImpl(String s, String other) {
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
