package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base SuggestBox widget that adapts to UiCommon list model items.
 */
public abstract class BaseListModelSuggestBox<T> extends Composite implements EditorWidget<T, TakesConstrainedValueEditor<T>>, HasConstrainedValue<T> {

    private TakesConstrainedValueEditor<T> editor;

    private T value;

    private SuggestBox suggestBox;

    private ListModelSuggestionDisplay suggestionDisplay;

    public BaseListModelSuggestBox(MultiWordSuggestOracle suggestOracle) {
        this(suggestOracle, 445);
    }

    public BaseListModelSuggestBox(MultiWordSuggestOracle suggestOracle, int maxSuggestionPanelHeightInPx) {
        suggestionDisplay = new ListModelSuggestionDisplay(maxSuggestionPanelHeightInPx);
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

    protected boolean isSuggestionListShowing() {
        return suggestionDisplay.isSuggestionListShowing();
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

    public Widget getSuggestionMenu() {
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
        return asSuggestBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                try {
                    T value = asEntity(event.getValue());
                    handler.onValueChange(new ValueChangeEvent<T>(value) {});
                } catch (IllegalArgumentException e) {
                    // ignore - the user entered an incorrect string. Just do not notify the listeners
                }
            }
        });
    }

    class ListModelSuggestionDisplay extends DefaultSuggestionDisplay {

        private Widget suggestionMenu;

        public ListModelSuggestionDisplay(int maxSuggestionPanelHeightInPx) {
            getPopupPanel().getElement().getStyle().setZIndex(1);

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

        public Widget getSuggestionMenu() {
            return suggestionMenu;
        }

        @Override
        protected Widget decorateSuggestionList(Widget suggestionList) {
            this.suggestionMenu = suggestionList;
            return super.decorateSuggestionList(suggestionList);
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

        @Override
        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
                                       boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
                                       SuggestBox.SuggestionCallback callback) {
            super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
            fixIe9Scrollbar();
        }

        /**
         * IE9 hack solving wrong scrollbar rendering.
         * ie9 counts the width of block without scrollbar event if the scrollbar should be visible, then
         * the scrollbar is show, content shifted to left and trimmed on left side.
         * Resetting 'overflow-y' to 'hidden' and back to 'scroll' prevents the shift to left, extending the width
         * adds space for scrollbar.
         *
         * <p>To be dropped together with IE9 support.</p>
         *
         * @see <a href="https://bugzilla.redhat.com/show_bug.cgi?id=1160774">Bug 1160774</a>
         */
        private void fixIe9Scrollbar() {
            boolean isIe9 = Window.Navigator.getUserAgent().contains("MSIE 9.0"); //$NON-NLS-1$
            if (isIe9 && super.isSuggestionListShowing()) {
                final Style suggestionMenuStyle = getSuggestionMenu().getElement().getStyle();
                final Overflow originalOverflowY = valueOfOverflow(
                        getComputedOverflowY(getSuggestionMenu().getElement()), Overflow.SCROLL);
                suggestionMenuStyle.setOverflowY(Overflow.HIDDEN);
                suggestionMenuStyle.setOverflowY(originalOverflowY);
                final int scrollbarWidthEstimate = 32; // px
                suggestionMenuStyle.setProperty("width", "");  //$NON-NLS-1$ $NON-NLS-2$
                suggestionMenuStyle.setWidth(
                        getSuggestionMenu().getElement().getClientWidth() + scrollbarWidthEstimate,
                        Unit.PX);
            }
        }

        private native String getComputedOverflowY(final Element element) /*-{
            return $wnd.getComputedStyle(element).overflowY;
        }-*/;

        private Overflow valueOfOverflow(String value, Overflow defaultValue) {
            for (Overflow overflow : Overflow.values()) {
                if (overflow.getCssName().equals(value)) {
                    return  overflow;
                }
            }
            return defaultValue;
        }
    }
}
