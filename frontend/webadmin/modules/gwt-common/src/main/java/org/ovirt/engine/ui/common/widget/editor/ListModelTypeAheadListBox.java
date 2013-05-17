package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor.SuggestBoxRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
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

    @UiField(provided = true)
    SuggestBox suggestBox;

    @UiField
    Image dropDownImage;

    @UiField
    FlowPanel mainPanel;

    @UiField
    Style style;

    private final SuggestBoxRenderer<T> renderer;

    private Collection<T> acceptableValues = new ArrayList<T>();

    private HandlerRegistration eventHandler;

    interface Style extends CssResource {

        String enabledMainPanel();

        String disabledMainPanel();
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, ListModelTypeAheadListBox> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public ListModelTypeAheadListBox(SuggestBoxRenderer<T> renderer) {
        super(new RenderableSuggestOracle<T>(renderer));
        this.renderer = renderer;

        suggestBox = asSuggestBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        registerListeners();
    }

    private void registerListeners() {
        dropDownImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                showAllSuggestions();
            }
        });

        // not listening to focus because it would show the suggestions also after the whole browser
        // gets the focus back (after loosing it) if this was the last element with focus
        suggestBox.getTextBox().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showAllSuggestions();
            }
        });

        // make sure that after leaving the text box a valid value or empty value will be rendered in the text box
        suggestBox.getTextBox().addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if (eventHandler != null) {
                    eventHandler.removeHandler();
                }

                adjustSelectedValue();
            }

        });

        suggestBox.getTextBox().addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                eventHandler =
                        Event.addNativePreviewHandler(new EnterIgnoringNativePreviewHandler<T>(ListModelTypeAheadListBox.this));
            }
        });

    }

    private void showAllSuggestions() {
        if (!isEnabled()) {
            return;
        }

        // show all the suggestions even if there is already something filled
        // otherwise it is not obvious that there are more options
        suggestBox.setText(null);
        suggestBox.showSuggestionList();
        setFocus(true);
    }

    private void adjustSelectedValue() {
        if (acceptableValues.size() == 0) {
            return;
        }

        // empty suggest box
        String providedText = asSuggestBox().getText();
        if (providedText == null || "".equals(providedText)) {
            if (getValue() != null) {
                // something has been there, deleted on click inside and than hidden the box - restoring
                asSuggestBox().setText(renderer.getReplacementString(getValue()));
            } else {
                // nothing has been there, selecting the first accpetable value
                setValue(acceptableValues.iterator().next());
            }
        } else {
            // something has been typed inside - validate
            T newData = asEntity(providedText);
            if (newData != null) {
                // correct provided - use it
                setValue(newData);
            } else {
                // incorrect - return to previous one
                asSuggestBox().setText(renderer.getReplacementString(getValue()));
            }
        }
    }

    @Override
    protected T asEntity(String provided) {
        if (provided == null) {
            return null;
        }

        for (T data : acceptableValues) {
            if (data == null) {
                continue;
            }

            String expected = renderer.getReplacementString(data);
            if (expected == null) {
                continue;
            }

            if (expected.equals(provided)) {
                return data;
            }
        }

        return null;
    }

    public void setValue(T value) {
        super.setValue(asValidValue(value));
    }

    public void setValue(T value, boolean fireEvents) {
        super.setValue(asValidValue(value), fireEvents);
    }

    @Override
    public T getValue() {
        if (acceptableValues.contains(super.getValue())) {
            return super.getValue();
        }

        return null;
    }

    private T asValidValue(T valueCandidate) {
        // not yet inited - accept everything
        if (acceptableValues.size() == 0) {
            return valueCandidate;
        }

        // it is one of the correct values
        if (acceptableValues.contains(valueCandidate)) {
            return valueCandidate;
        }

        // not correct value - return to the previous one
        return getValue();
    }

    @Override
    public void setAcceptableValues(Collection<T> acceptableValues) {
        this.acceptableValues = acceptableValues;
        RenderableSuggestOracle<T> suggestOracle = (RenderableSuggestOracle<T>) suggestBox.getSuggestOracle();
        suggestOracle.setData(acceptableValues);
    }

    @Override
    protected void render(T value, boolean fireEvents) {
        asSuggestBox().setValue(renderer.getReplacementString(value), fireEvents);
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
}

class RenderableSuggestion<T> extends MultiWordSuggestion {

    public RenderableSuggestion(T row, SuggestBoxRenderer<T> renderer) {
        super(renderer.getReplacementString(row), renderer.getDisplayString(row));
    }

    public boolean matches(String query) {
        String replacementString = getReplacementString();
        if (replacementString == null || query == null) {
            return false;
        }

        return replacementString.toLowerCase().startsWith(query.toLowerCase());
    }
}

class RenderableSuggestOracle<T> extends MultiWordSuggestOracle {

    private SuggestBoxRenderer<T> renderer;

    // inited to avoid null checks
    private Collection<T> data = new ArrayList<T>();

    public RenderableSuggestOracle(SuggestBoxRenderer<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        List<RenderableSuggestion<T>> suggestions = new ArrayList<RenderableSuggestion<T>>();

        String query = request.getQuery();
        for (T row : data) {
            RenderableSuggestion<T> suggestionCandidate = new RenderableSuggestion<T>(row, renderer);
            if (suggestionCandidate.matches(query)) {
                suggestions.add(suggestionCandidate);
            }
        }

        callback.onSuggestionsReady(request, new Response(suggestions));
    }

    @Override
    public void requestDefaultSuggestions(Request request, Callback callback) {
        List<RenderableSuggestion<T>> suggestions = new ArrayList<RenderableSuggestion<T>>();

        for (T row : data) {
            suggestions.add(new RenderableSuggestion<T>(row, renderer));
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
        if (nativeEvent.getKeyCode() == KeyCodes.KEY_ENTER) {
            // swallow the enter key otherwise the whole dialog would get submitted
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
            event.cancel();

            // process the event here directly
            Suggestion currentSelection = listModelTypeAheadListBox.getCurrentSelection();
            if (currentSelection != null) {
                String replacementString = currentSelection.getReplacementString();
                listModelTypeAheadListBox.setValue(listModelTypeAheadListBox.asEntity(replacementString), true);
            }

            listModelTypeAheadListBox.hideSuggestions();
        }
    }

}
