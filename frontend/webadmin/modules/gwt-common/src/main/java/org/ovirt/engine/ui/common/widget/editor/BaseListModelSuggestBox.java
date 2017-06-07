package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * Base SuggestBox widget that adapts to UiCommon list model items.
 */
public abstract class BaseListModelSuggestBox<T> extends Composite implements
    EditorWidget<T, TakesConstrainedValueEditor<T>>, HasConstrainedValue<T>, HasCleanup {

    private TakesConstrainedValueEditor<T> editor;

    private T value;

    @UiField(provided = true)
    protected SuggestBox suggestBox;

    private ListModelSuggestionDisplay suggestionDisplay;

    protected List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public BaseListModelSuggestBox(MultiWordSuggestOracle suggestOracle) {
        this(suggestOracle, 445);
    }

    public BaseListModelSuggestBox(MultiWordSuggestOracle suggestOracle, int maxSuggestionPanelHeightInPx) {
        suggestionDisplay = new ListModelSuggestionDisplay(maxSuggestionPanelHeightInPx);
        suggestBox = new SuggestBox(suggestOracle, new TextBox(), suggestionDisplay);

        suggestBox.removeStyleName("gwt-SuggestBox"); //$NON-NLS-1$

        handlerRegistrations.add(suggestBox.addSelectionHandler(event -> ValueChangeEvent.fire(suggestBox, event.getSelectedItem().getReplacementString())));

    }

    protected Suggestion getCurrentSelection() {
        return suggestionDisplay.getCurrentSelection();
    }

    protected void hideSuggestions() {
        suggestionDisplay.hide();
    }

    protected boolean isSuggestionListShowing() {
        return suggestionDisplay.isSuggestionListShowing();
    }

    @Override
    public TakesConstrainedValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
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
        HandlerRegistration handlerRegistration = asSuggestBox().addKeyUpHandler(handler);
        handlerRegistrations.add(handlerRegistration);
        return handlerRegistration;
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        HandlerRegistration handlerRegistration = asSuggestBox().addKeyDownHandler(handler);
        handlerRegistrations.add(handlerRegistration);
        return handlerRegistration;
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        HandlerRegistration handlerRegistration = asSuggestBox().addKeyPressHandler(handler);
        handlerRegistrations.add(handlerRegistration);
        return handlerRegistration;
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
        asSuggestBox().getValueBox().setFocus(focused);
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

    public MenuBar getSuggestionMenu() {
        return suggestionDisplay.getSuggestionMenu();
    }

    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        this.value = value;
        render(value, fireEvents);
    }

    public void setAutoHideEnabled(boolean enabled) {
        suggestionDisplay.setAutoHideEnabled(enabled);
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
     * @return the entity representation
     * @throws IllegalArgumentException if incorrect value has been passed
     */
    protected abstract T asEntity(String value);

    protected void scrollSelectedItemIntoView() {

    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
        HandlerRegistration handlerRegistration = asSuggestBox().addValueChangeHandler(event -> {
            try {
                T value = asEntity(event.getValue());
                handler.onValueChange(new ValueChangeEvent<T>(value) {});
            } catch (IllegalArgumentException e) {
                // ignore - the user entered an incorrect string. Just do not notify the listeners
            }
        });
        handlerRegistrations.add(handlerRegistration);
        return handlerRegistration;
    }

    @Override
    public void cleanup() {
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
    }

    class ListModelSuggestionDisplay extends DefaultSuggestionDisplay {

        public ListModelSuggestionDisplay(int maxSuggestionPanelHeightInPx) {
            getPopupPanel().getElement().getStyle().setZIndex(PatternflyConstants.ZINDEX_MODAL + 1);

            Element popupPanelElement = getPopupPanel().getWidget().getElement();
            Style popupPanel = popupPanelElement.getStyle();
            popupPanel.setDisplay(Style.Display.BLOCK);
            popupPanel.setProperty("height", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
            popupPanel.setPropertyPx("maxHeight", maxSuggestionPanelHeightInPx); //$NON-NLS-1$
            popupPanel.setOverflowY(Overflow.SCROLL);
            popupPanel.setOverflowX(Overflow.HIDDEN);

            Style suggestPopupContentStyle = popupPanelElement.getParentElement().getStyle();
            suggestPopupContentStyle.setProperty("height", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
            suggestPopupContentStyle.setPropertyPx("maxHeight", maxSuggestionPanelHeightInPx); //$NON-NLS-1$
            suggestPopupContentStyle.setOverflowX(Overflow.HIDDEN);
            suggestPopupContentStyle.setProperty("display", "table"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // just to make it public
        @Override
        public Suggestion getCurrentSelection() {
            return super.getCurrentSelection();
        }

        public void hide() {
            getPopupPanel().hide();
        }

        public void setAutoHideEnabled(boolean enabled) {
            getPopupPanel().setAutoHideEnabled(enabled);
        }

        public MenuBar getSuggestionMenu() {
            return super.getSuggestionMenu();
        }

        @Override
        protected void moveSelectionUp() {
            super.moveSelectionUp();

            scrollSelectedItemIntoView();
        }

        @Override
        protected void moveSelectionDown() {
            super.moveSelectionDown();

            scrollSelectedItemIntoView();
        }

    }

}
