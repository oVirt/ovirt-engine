package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor.SuggestBoxRenderer;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * SuggestBox widget that adapts to UiCommon list model items and looks like a list box. The suggestion content can be rich (html).
 * <p>
 * Accepts any objects as soon as the provided renderer can render them.
 */
public class ListModelTypeAheadListBox<T> extends BaseListModelSuggestBox<T> {

    private static final String LMTALB_LISTBOX_PFLY_FIX = "lmtalb_listbox_pfly_fix"; //$NON-NLS-1$
    private static final String LMTALB_WRAPPER_LISTBOX_PFLY_FIX = "lmtalb_suggestboxWrapper_pfly_fix"; //$NON-NLS-1$
    private static final String LMTALB_SUGGESTBOX_PFLY_FIX = "lmtalb_suggestbox_pfly_fix"; //$NON-NLS-1$
    private static final String PATTERNFLY_IMAGE_HEIGHT = "26px"; //$NON-NLS-1$

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField(provided = true)
    SuggestBox suggestBox;

    @UiField
    Image dropDownImage;

    @UiField
    FlowPanel mainPanel;

    @UiField
    Style style;

    private final SuggestBoxRenderer<T> renderer;

    /**
     * This is used to decide whether, when setting the value of the widget to one that doesn't exist among the list of
     * suggested items {@link #acceptableValues}, the new value should be added to it; see usage in
     * {@link #addToValidValuesIfNeeded(Object)}.
     */
    private final boolean autoAddToValidValues;

    private Collection<T> acceptableValues = new ArrayList<>();

    private HandlerRegistration eventHandler;

    interface Style extends CssResource {

        String enabledMainPanel();

        String disabledMainPanel();

        String suggestBoxStyle_legacy();

        String suggestBoxWrapperStyle_legacy();

        String maxWidth();
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, ListModelTypeAheadListBox<?>> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public ListModelTypeAheadListBox(SuggestBoxRenderer<T> renderer, boolean autoAddToValidValues,
                                     SuggestionMatcher suggestionMatcher) {
        super(new RenderableSuggestOracle<>(renderer, suggestionMatcher));
        this.renderer = renderer;
        this.autoAddToValidValues = autoAddToValidValues;

        suggestBox = asSuggestBox();

        // this needs to be handled by focus on text box and clicks on drop down image
        setAutoHideEnabled(false);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        mainPanel.addStyleName(LMTALB_LISTBOX_PFLY_FIX);

        registerListeners();
    }

    public void setUsePatternFly(final boolean usePatternFly) {
        if (usePatternFly) {
            mainPanel.removeStyleName(LMTALB_LISTBOX_PFLY_FIX);
            mainPanel.removeStyleName(Styles.FORM_CONTROL);
            mainPanel.addStyleName(style.maxWidth());
            suggestBox.removeStyleName(style.suggestBoxStyle_legacy());
            suggestBox.removeStyleName(LMTALB_SUGGESTBOX_PFLY_FIX);
            suggestBox.addStyleName(Styles.FORM_CONTROL);
            suggestBox.getParent().removeStyleName(LMTALB_WRAPPER_LISTBOX_PFLY_FIX);
            suggestBox.getParent().removeStyleName(style.suggestBoxWrapperStyle_legacy());
            dropDownImage.setHeight(PATTERNFLY_IMAGE_HEIGHT);
        }
    }

    private void registerListeners() {
        SuggestBoxFocusHandler handlers = new SuggestBoxFocusHandler();
        suggestBox.getValueBox().addBlurHandler(handlers);
        suggestBox.getValueBox().addFocusHandler(handlers);

        // not listening to focus because it would show the suggestions also after the whole browser
        // gets the focus back (after loosing it) if this was the last element with focus
        suggestBox.getValueBox().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switchSuggestions();
            }
        });

        dropDownImage.addMouseDownHandler(new FocusHandlerEnablingMouseHandlers(handlers));
        dropDownImage.addMouseUpHandler(new FocusHandlerEnablingMouseHandlers(handlers) {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                super.onMouseUp(event);
                switchSuggestions();
            }
        });

        getSuggestionMenu().getParent().addDomHandler(new FocusHandlerEnablingMouseHandlers(handlers), MouseDownEvent.getType());

        // no need to do additional switchSuggestions() - it is processed by MenuBar itself
        getSuggestionMenu().getParent().addDomHandler(new FocusHandlerEnablingMouseHandlers(handlers), MouseUpEvent.getType());

        addValueChangeHandler(new ValueChangeHandler<T>() {
            @Override
            public void onValueChange(ValueChangeEvent<T> event) {
                // if the value has been changed using the mouse
                setValue(event.getValue());
            }
        });

    }

    protected void switchSuggestions() {
        if (!isEnabled()) {
            return;
        }

        if (isSuggestionListShowing()) {
            hideSuggestions();
            adjustSelectedValue();
        } else {
            showAllSuggestions();
        }
    }

    protected void showAllSuggestions() {
        // show all the suggestions even if there is already something filled
        // otherwise it is not obvious that there are more options
        suggestBox.setText(null);
        suggestBox.showSuggestionList();

        selectInMenu(getValue());

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute () {
                setFocus(true);
            }
        });
    }

    protected void selectInMenu(T toSelect) {
        if (!(getSuggestionMenu() instanceof MenuBar)) {
            // can not select if the it is not a menu bar
            return;
        }

        MenuBar menuBar = (MenuBar) getSuggestionMenu();
        List<MenuItem> items = getItems(menuBar);
        if (items == null) {
            return;
        }

        String selectedReplacementString = renderer.getReplacementString(toSelect);
        if (selectedReplacementString == null) {
            return;
        }

        int selectedItemIndex = -1;
        for (T acceptableValue : acceptableValues) {
            selectedItemIndex ++;
            String acceptableValueReplacement = renderer.getReplacementString(acceptableValue);
            if (acceptableValueReplacement != null && acceptableValueReplacement.equals(selectedReplacementString)) {
                if (items.size() > selectedItemIndex) {
                    menuBar.selectItem(items.get(selectedItemIndex));
                    scrollSelectedItemIntoView();
                }

                break;
            }
        }
    }

    protected void scrollSelectedItemIntoView() {
        if (!(getSuggestionMenu() instanceof MenuBar)) {
            // can not select if it is not a menu bar
            return;
        }

        MenuBar menuBar = (MenuBar) getSuggestionMenu();
        MenuItem item = getSelectedItem(menuBar);
        if (item != null) {
            Element toSelect = item.getElement().getParentElement();
            toSelect.scrollIntoView();
        }
    }

    // extremely ugly - there is just no better way how to find the items as MenuItems
    private native List<MenuItem> getItems(MenuBar menuBar) /*-{
        return menuBar.@com.google.gwt.user.client.ui.MenuBar::getItems()();
    }-*/;

    private native MenuItem getSelectedItem(MenuBar menuBar) /*-{
        return menuBar.@com.google.gwt.user.client.ui.MenuBar::getSelectedItem()();
    }-*/;

    protected void adjustSelectedValue() {
        if (acceptableValues.size() == 0) {
            return;
        }

        String providedText = asSuggestBox().getText();
        // validate input text
        try {
            T newData = asEntity(providedText);
            // correct provided - use it
            setValue(newData);
        } catch (IllegalArgumentException e) {
            // incorrect - return to previous one
            render(getValue(), false);
        }
    }

    @Override
    protected T asEntity(String provided) {
        if (provided == null) {
            if (acceptableValues.contains(null)) {
                return null;
            } else {
                throw new IllegalArgumentException("Only non-null arguments are accepted"); //$NON-NLS-1$
            }
        }

        for (T data : acceptableValues) {
            String expected = renderer.getReplacementString(data);
            if (expected == null) {
                continue;
            }

            if (expected.equals(provided)) {
                return data;
            }
        }

        throw new IllegalArgumentException("The provided value is not acceptable: '" + provided + "'"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setValue(T value) {
        addToValidValuesIfNeeded(value);
        super.setValue(value);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        addToValidValuesIfNeeded(value);
        super.setValue(value, fireEvents);
    }

    @Override
    public T getValue() {
        if (super.getValue() != null && acceptableValues.contains(super.getValue())) {
            return super.getValue();
        }

        return null;
    }

    private void addToValidValuesIfNeeded(T value) {
        if (value != null && !acceptableValues.contains(value) && autoAddToValidValues) {
            acceptableValues.add(value);
        }

    }

    @Override
    public void setAcceptableValues(Collection<T> acceptableValues) {
        this.acceptableValues = acceptableValues;
        T selected = getValue();
        addToValidValuesIfNeeded(selected);
        RenderableSuggestOracle<T> suggestOracle = (RenderableSuggestOracle<T>) suggestBox.getSuggestOracle();
        suggestOracle.setData(acceptableValues);
    }

    @Override
    protected void render(T value, boolean fireEvents) {
        String replacementString = renderer.getReplacementString(value);
        boolean empty = StringUtils.isEmpty(replacementString);
        asSuggestBox().setValue(empty ? constants.emptyListBoxText() : replacementString, fireEvents);
        grayOutPlaceholderText(empty);
    }

    protected void grayOutPlaceholderText(boolean isPlaceholder) {
        if (isPlaceholder) {
            asSuggestBox().getElement().getStyle().setColor("gray"); //$NON-NLS-1$
        } else {
            asSuggestBox().getElement().getStyle().clearColor();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            mainPanel.getElement().replaceClassName(style.disabledMainPanel(), style.enabledMainPanel());
        } else {
            mainPanel.getElement().replaceClassName(style.enabledMainPanel(), style.disabledMainPanel());
        }
    }

    class SuggestBoxFocusHandler implements FocusHandler, BlurHandler {

        private boolean enabled = true;

        @Override
        public void onBlur(BlurEvent blurEvent) {
            if (eventHandler != null) {
                eventHandler.removeHandler();
                eventHandler = null;
            }

            // process only if it will not be processed by other handlers
            if (enabled) {
                // first give the opportunity to the click handler on the menu to process the event, than we can hide it
                hideSuggestions();
                adjustSelectedValue();
            }
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void onFocus(FocusEvent event) {
            if (eventHandler != null) {
                eventHandler =
                        Event.addNativePreviewHandler(new EnterIgnoringNativePreviewHandler<>(ListModelTypeAheadListBox.this));
            }
        }
    }

    class FocusHandlerEnablingMouseHandlers implements MouseDownHandler, MouseUpHandler {

        private SuggestBoxFocusHandler focusHandler;

        public FocusHandlerEnablingMouseHandlers(SuggestBoxFocusHandler focusHandler) {
            this.focusHandler = focusHandler;
        }

        @Override
        public void onMouseDown(MouseDownEvent event) {
            focusHandler.setEnabled(false);
        }

        @Override
        public void onMouseUp(MouseUpEvent event) {
            focusHandler.setEnabled(true);
        }
    }

}

class RenderableSuggestion<T> extends MultiWordSuggestion {

    public RenderableSuggestion(T row, SuggestBoxRenderer<T> renderer) {
        super(renderer.getReplacementString(row), renderer.getDisplayString(row));
    }
}

class RenderableSuggestOracle<T> extends MultiWordSuggestOracle {

    private SuggestBoxRenderer<T> renderer;
    private final SuggestionMatcher matcher;

    // inited to avoid null checks
    private Collection<T> data = new ArrayList<>();

    public RenderableSuggestOracle(SuggestBoxRenderer<T> renderer, SuggestionMatcher matcher) {
        this.renderer = renderer;
        this.matcher = matcher;
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        List<RenderableSuggestion<T>> suggestions = new ArrayList<>();

        String query = request.getQuery();
        for (T row : data) {
            RenderableSuggestion<T> suggestionCandidate = new RenderableSuggestion<>(row, renderer);
            if (this.matcher.match(query, suggestionCandidate)) {
                suggestions.add(suggestionCandidate);
            }
        }

        callback.onSuggestionsReady(request, new Response(suggestions));
    }

    @Override
    public void requestDefaultSuggestions(Request request, Callback callback) {
        List<RenderableSuggestion<T>> suggestions = new ArrayList<>();

        for (T row : data) {
            suggestions.add(new RenderableSuggestion<>(row, renderer));
        }

        callback.onSuggestionsReady(request, new Response(suggestions));
    }

    public void setData(Collection<T> data) {
        this.data = data;
    }

}

class EnterIgnoringNativePreviewHandler<T> implements NativePreviewHandler {

    private final ListModelTypeAheadListBox<T> listModelTypeAheadListBox;

    public EnterIgnoringNativePreviewHandler(ListModelTypeAheadListBox<T> listModelTypeAheadListBox) {
        this.listModelTypeAheadListBox = listModelTypeAheadListBox;
    }

    @Override
    public void onPreviewNativeEvent(NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (nativeEvent.getKeyCode() == KeyCodes.KEY_ENTER && event.getTypeInt() == Event.ONKEYPRESS && !event.isCanceled()) {
            // swallow the enter key otherwise the whole dialog would get submitted
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
            event.cancel();

            // process the event here directly
            Suggestion currentSelection = listModelTypeAheadListBox.getCurrentSelection();
            if (currentSelection != null) {
                String replacementString = currentSelection.getReplacementString();
                try {
                    listModelTypeAheadListBox.setValue(listModelTypeAheadListBox.asEntity(replacementString), true);
                } catch (IllegalArgumentException e) {
                    // do not set the value if it is not a correct one
                }
            }

            listModelTypeAheadListBox.hideSuggestions();
        }
    }

}
