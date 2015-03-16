package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.user.client.ui.HasConstrainedValue;

/**
 * EntityModel bound Gwt-Bootstrap DateTimePicker that uses {@link GwtBootstrapDateTimePicker}.
 */
public class EntityModelDateTimeBox extends GwtBootstrapDateTimePicker implements EditorWidget<Date, TakesValueEditor<Date>>, HasConstrainedValue<Date> {

    private TakesConstrainedValueEditor<Date> editor;

    private int tabIndex;

    private boolean enabled;

    private char accessKey;

    public EntityModelDateTimeBox(String format, boolean autoClose) {
        super(format, autoClose);
    }

    public EntityModelDateTimeBox() {
        super();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getTabIndex() {
        return tabIndex;
    }

    @Override
    public void setAccessKey(char key) {
        accessKey = key;
    }

    @Override
    public void setTabIndex(int index) {
        this.tabIndex = index;
    }

    public char getAccessKey() {
        return accessKey;
    }

    @Override
    public TakesValueEditor<Date> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    @Override
    public void setAcceptableValues(Collection<Date> values) {
        if (values instanceof List<?>) {
            List<Date> allowedDates = (List<Date>) values;
            Collections.sort(allowedDates);
            setDateRange(allowedDates.get(0), allowedDates.get(allowedDates.size() - 1));
        }
    }
}
