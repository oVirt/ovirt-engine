package org.ovirt.engine.ui.uicompat;


public interface Translator<T> {

    public String translate(T key);

    boolean containsKey(T key);
}
