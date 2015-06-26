package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import com.google.gwt.user.client.TakesValue;

public interface TakesListValue<T> extends TakesValue<List<T>> {
    void setListValue(List<T> value);
}
