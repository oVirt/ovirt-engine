package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Collection;

import org.ovirt.engine.ui.common.widget.editor.BaseListModelSuggestBox;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 * SuggestBox widget that adapts to UiCommon list model items. Expects all of it's items to be non null Strings
 */
public class ListModelSuggestBox extends BaseListModelSuggestBox<String> {

    public ListModelSuggestBox() {
        super(new MultiWordSuggestOracle());
        initWidget(asSuggestBox());

        handlerRegistrations.add(asSuggestBox().getValueBox()
                .addFocusHandler(event -> asSuggestBox().showSuggestionList()));
        handlerRegistrations.add(Event.addNativePreviewHandler(event -> {
            if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
                // By default SuggestBox applies selection upon tab press - this is bad
                event.getNativeEvent().stopPropagation();
                hideSuggestions();
            }
        }));
    }

    @Override
    public void setAcceptableValues(Collection<String> values) {
        MultiWordSuggestOracle suggestOracle = (MultiWordSuggestOracle) asSuggestBox().getSuggestOracle();
        suggestOracle.clear();
        suggestOracle.addAll(values);
        suggestOracle.setDefaultSuggestionsFromText(values);
    }

    @Override
    protected void render(String value, boolean fireEvents) {
        asSuggestBox().setValue(value, fireEvents);
    }

    @Override
    protected String asEntity(String value) {
        return value;
    }
}
