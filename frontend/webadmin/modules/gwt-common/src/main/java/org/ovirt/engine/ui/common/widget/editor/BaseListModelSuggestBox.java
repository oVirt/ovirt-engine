package org.ovirt.engine.ui.common.widget.editor;

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
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * Base SuggestBox widget that adapts to UiCommon list model items.
 */
public abstract class BaseListModelSuggestBox<T> extends Composite implements EditorWidget<T, TakesConstrainedValueEditor<T>>, HasConstrainedValue<T> {

    private TakesConstrainedValueEditor<T> editor;

    private T value;

    private SuggestBox suggestBox;

    private ListModelSuggestionDisplay suggestionDisplay = new ListModelSuggestionDisplay();

    public BaseListModelSuggestBox(MultiWordSuggestOracle suggestOracle) {
        suggestBox = new SuggestBox(suggestOracle, new TextBox(), suggestionDisplay);

        suggestBox.removeStyleName("gwt-SuggestBox"); //$NON-NLS-1$

        suggestBox.addSelectionHandler(new SelectionHandler<Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                ValueChangeEvent.fire(suggestBox, event.getSelectedItem().getReplacementString());
            }
        });

    }

    protected Suggestion getCurrentSelection() {
        return suggestionDisplay.getCurrentSelection();
    }

    protected void hideSuggestions() {
        suggestionDisplay.hide();
    }

    @Override
    public TakesConstrainedValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.<T> of(this, this, this);
        }
        return editor;
    }

    public SuggestBox asSuggestBox() {
        return suggestBox;
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

    public void setValue(T value) {
        setValue(value, false);
    }

    public void setValue(T value, boolean fireEvents) {
        this.value = value;
        render(value, fireEvents);
    }

    /**
     * Renders the value to the suggest box as a String
     * @param value the current value
     * @param fireEvents if the suggest box should fire events
     */
    protected abstract void render(T value, boolean fireEvents);

    /**
     * Returns the entity representation of the given String which was passed to the suggest box.
     * @param value String from the suggest box
     * @return the entity representation, or null if incorrect value passed
     */
    protected abstract T asEntity(String value);

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
        return asSuggestBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                T value = asEntity(event.getValue());
                if (value != null) {
                    handler.onValueChange(new ValueChangeEvent<T>(value) {
                    });
                }
            }
        });
    }

    class ListModelSuggestionDisplay extends DefaultSuggestionDisplay {

        public ListModelSuggestionDisplay() {
            // not be hidden under the panel
            getPopupPanel().getElement().getStyle().setZIndex(1);
        }

        // just to make it public
        @Override
        public Suggestion getCurrentSelection() {
            return super.getCurrentSelection();
        }

        public void hide() {
            getPopupPanel().hide();
        }
    }
}
