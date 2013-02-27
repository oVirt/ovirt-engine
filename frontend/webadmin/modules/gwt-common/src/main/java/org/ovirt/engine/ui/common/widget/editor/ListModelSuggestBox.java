package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.Linq;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * SuggestBox widget that adapts to UiCommon list model items.
 */
public class ListModelSuggestBox extends Composite implements EditorWidget<Object, TakesConstrainedValueEditor<Object>>, HasConstrainedValue<Object> {

    private TakesConstrainedValueEditor<Object> editor;
    private final MultiWordSuggestOracle suggestOracle = new MultiWordSuggestOracle();

    public ListModelSuggestBox() {
        final SuggestBox suggestBox = new SuggestBox(suggestOracle);
        suggestBox.removeStyleName("gwt-SuggestBox"); //$NON-NLS-1$
        initWidget(suggestBox);

        suggestBox.addSelectionHandler(new SelectionHandler<Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                ValueChangeEvent.fire(suggestBox, event.getSelectedItem().getReplacementString());
            }
        });

        suggestBox.getTextBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                suggestBox.showSuggestionList();
            }
        });
    }

    @Override
    public TakesConstrainedValueEditor<Object> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    public SuggestBox asSuggestBox() {
        return (SuggestBox) getWidget();
    }

    public TextBoxBase asTextBox() {
        return asSuggestBox().getTextBox();
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return asSuggestBox().addKeyUpHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return asSuggestBox().addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return asSuggestBox().addKeyPressHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return asSuggestBox().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        asSuggestBox().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        asSuggestBox().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        asSuggestBox().setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return asTextBox().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        asTextBox().setEnabled(enabled);
    }

    @Override
    public Object getValue() {
        return asSuggestBox().getValue();
    }

    @Override
    public void setValue(Object value) {
        asSuggestBox().setValue((String) value);
    }

    @Override
    public void setValue(Object value, boolean fireEvents) {
        asSuggestBox().setValue((String) value, fireEvents);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Object> handler) {
        return asSuggestBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                handler.onValueChange(new ValueChangeEvent<Object>(event.getValue()) {});
            }
        });
    }

    @Override
    public void setAcceptableValues(Collection<Object> values) {
        Collection<String> stringValues = Linq.Cast(values);
        suggestOracle.clear();
        suggestOracle.addAll(stringValues);
        suggestOracle.setDefaultSuggestionsFromText(stringValues);
    }

}
