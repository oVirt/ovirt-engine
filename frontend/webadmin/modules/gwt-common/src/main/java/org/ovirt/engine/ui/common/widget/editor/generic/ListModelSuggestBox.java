package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.Collection;

import org.ovirt.engine.ui.common.widget.editor.BaseListModelSuggestBox;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

/**
 * SuggestBox widget that adapts to UiCommon list model items. Expects all of it's items to be non null Strings
 */
public class ListModelSuggestBox extends BaseListModelSuggestBox<String> {

    public ListModelSuggestBox() {
        super(new MultiWordSuggestOracle());
        initWidget(asSuggestBox());

        asSuggestBox().getValueBox().addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {
                        asSuggestBox().showSuggestionList();
                    }
                });
            }
        });
        addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {
                        ValueChangeEvent.fire(asSuggestBox(), asSuggestBox().getText());
                    }
                });
            }
        });
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_TAB) {
                    // By default SuggestBox applies selection upon tab press - this is bad
                    event.getNativeEvent().stopPropagation();
                    hideSuggestions();
                }
            }
        });
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
