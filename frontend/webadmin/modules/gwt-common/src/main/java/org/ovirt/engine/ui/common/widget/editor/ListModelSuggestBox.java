package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.Linq;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 * SuggestBox widget that adapts to UiCommon list model items. Expects all of it's items to be non null Strings
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBox instead
 */
@Deprecated
public class ListModelSuggestBox extends BaseListModelSuggestBox<Object> {

    public ListModelSuggestBox() {
        super(new MultiWordSuggestOracle());
        initWidget(asSuggestBox());

        asSuggestBox().getTextBox().addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                asSuggestBox().showSuggestionList();
            }
        });
    }

    @Override
    public void setAcceptableValues(Collection<Object> values) {
        Collection<String> stringValues = Linq.cast(values);
        MultiWordSuggestOracle suggestOracle = (MultiWordSuggestOracle) asSuggestBox().getSuggestOracle();
        suggestOracle.clear();
        suggestOracle.addAll(stringValues);
        suggestOracle.setDefaultSuggestionsFromText(stringValues);
    }

    @Override
    protected void render(Object value, boolean fireEvents) {
        asSuggestBox().setValue((String) value, fireEvents);
    }

    @Override
    protected Object asEntity(String value) {
        return value;
    }

}
