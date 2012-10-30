package org.ovirt.engine.ui.uicommonweb;

public interface ViewFilter<T> {
    String getText();

    T getValue();
}
