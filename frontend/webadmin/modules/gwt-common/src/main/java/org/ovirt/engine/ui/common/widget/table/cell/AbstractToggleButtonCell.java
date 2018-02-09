package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class AbstractToggleButtonCell<T> extends AbstractCell<T> {

    public interface ToggleButtonCellTemplate extends SafeHtmlTemplates {

        @Template("<span id=\"{0}\" style=\"padding-left: 1px;\">{1}</span>")
        public SafeHtml span(String id, SafeHtml html);

        @Template("<input id=\"{2}\" style=\"{1} background: transparent; border: 0px; text-align: center; width: 85%; "
                + "\" readonly=\"readonly\" type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
        public SafeHtml noButton(String value, SafeStyles customStyle, String id);

        @Template("<button id=\"{0}\" tabindex='-1' type=\"button\" style=\"border-radius:2px; color:black; " +
                "width:85%; padding-top:0; padding-bottom:0 ;padding-left:2px; padding-right:2px;\"" +
                "class=\"gwt-ToggleButton gwt-ToggleButton-up\" tabindex=\"-1\" aria-pressed=\"true\">{1}</button>")
        public SafeHtml toggledUp(String id, String value);

        @Template("<button id=\"{0}\" tabindex='-1' type=\"button\" style=\"border-radius:2px; color:black; " +
                "width:85%; padding-top:0; padding-bottom:0; padding-left:2px; padding-right:2px;\"" +
                "class=\"gwt-ToggleButton gwt-ToggleButton-down\" tabindex=\"-1\" aria-pressed=\"false\">{1}</button>")
        public SafeHtml toggledDown(String id, String value);
    }

    protected static final ToggleButtonCellTemplate templates = GWT.create(ToggleButtonCellTemplate.class);

    public AbstractToggleButtonCell() {
    }


    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
    }

    @Override
    public void onBrowserEvent(Context context,
                               Element parent,
                               T model,
                               NativeEvent event,
                               ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, model, event, valueUpdater);
        if (!BrowserEvents.CLICK.equals(event.getType())) {
            return;
        }
        onClickEvent(model);
    }

    public abstract void onClickEvent(T model);

}
