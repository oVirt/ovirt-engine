package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.VersionTransform;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SimpleKeyProvider;

/**
 * Refer to {@link com.google.gwt.user.client.ui.ValueListBox}, open the method {@link #addValue()} to set
 * {@link com.google.gwt.dom.client.SelectElement}'s option text and value independently.
 *
 * @param <T>
 *            the value type
 */
public class ValueListBox<T> extends Composite implements Focusable, HasConstrainedValue<T>, HasEnabled, IsEditor<TakesValueEditor<T>> {

    private final List<T> values = new ArrayList<T>();
    private final Map<Object, Integer> valueKeyToIndex = new HashMap<Object, Integer>();
    private final Renderer<T> renderer;
    private final ProvidesKey<T> keyProvider;

    private TakesValueEditor<T> editor;
    private T value;

    public ValueListBox(Renderer<T> renderer) {
        this(renderer, new SimpleKeyProvider<T>());
    }

    public ValueListBox(Renderer<T> renderer, ProvidesKey<T> keyProvider) {
        this.keyProvider = keyProvider;
        this.renderer = renderer;
        initWidget(new ListBox());

        getListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = getListBox().getSelectedIndex();

                if (selectedIndex < 0) {
                    return; // Not sure why this happens during addValue
                }
                T newValue = values.get(selectedIndex);
                setValue(newValue, true);
            }
        });
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns a {@link TakesValueEditor} backed by the ValueListBox.
     */
    @Override
    public TakesValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    @Override
    public int getTabIndex() {
        return getListBox().getTabIndex();
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isEnabled() {
        return getListBox().isEnabled();
    }

    @Override
    public void setAcceptableValues(Collection<T> newValues) {
        values.clear();
        valueKeyToIndex.clear();
        ListBox listBox = getListBox();
        listBox.clear();

        for (T nextNewValue : newValues) {
            addValue(nextNewValue);
        }

        updateListBox();

    }

    @Override
    public void setAccessKey(char key) {
        getListBox().setAccessKey(key);
    }

    @Override
    public void setEnabled(boolean enabled) {
        getListBox().setEnabled(enabled);
    }

    @Override
    public void setFocus(boolean focused) {
        getListBox().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        getListBox().setTabIndex(index);
    }

    /**
     * Set the value and display it in the select element. Add the value to the acceptable set if it is not already
     * there.
     */
    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }

        T before = this.value;
        this.value = value;
        updateListBox();

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    protected ListBox getListBox() {
        return (ListBox) getWidget();
    }

    protected void addValue(T value) {
        Object key = keyProvider.getKey(value);
        if (valueKeyToIndex.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate value: " + value);//$NON-NLS-1$
        }
        if (value instanceof Version) {
            Version version = (Version) value;
            valueKeyToIndex.put(key, values.size());
            values.add(value);
            // option: text, value
            getListBox().addItem(VersionTransform.getEayunVersion(version).getValue(), renderer.render(value));
        } else {
            valueKeyToIndex.put(key, values.size());
            values.add(value);
            getListBox().addItem(renderer.render(value));
        }
        assert values.size() == getListBox().getItemCount();
    }

    protected void addValue(String text, T value) {
        Object key = keyProvider.getKey(value);
        if (valueKeyToIndex.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate value: " + value);//$NON-NLS-1$
        }

        valueKeyToIndex.put(key, values.size());
        values.add(value);
        getListBox().addItem(text, renderer.render(value));
        assert values.size() == getListBox().getItemCount();
    }

    protected void updateListBox() {
        Object key = keyProvider.getKey(value);
        Integer index = valueKeyToIndex.get(key);
        if (index == null) {
            addValue(value);
        }

        index = valueKeyToIndex.get(key);
        getListBox().setSelectedIndex(index);
    }

    public List<T> getValues() {
        return values;
    }

    public ProvidesKey<T> getKeyProvider() {
        return keyProvider;
    }
}
