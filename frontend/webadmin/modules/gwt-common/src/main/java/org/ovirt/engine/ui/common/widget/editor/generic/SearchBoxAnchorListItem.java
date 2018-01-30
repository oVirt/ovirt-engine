package org.ovirt.engine.ui.common.widget.editor.generic;

import org.gwtbootstrap3.client.ui.AnchorListItem;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

public class SearchBoxAnchorListItem extends AnchorListItem {
    private ListModelSearchBox<?, ?> searchBox;

    SearchBoxAnchorListItem(ListModelSearchBox<?, ?> searchBox) {
        sinkEvents(Event.ONKEYUP);
        sinkEvents(Event.ONKEYDOWN);
        sinkEvents(Event.ONKEYPRESS);
        this.searchBox = searchBox;
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        if (Event.ONKEYDOWN == DOM.eventGetType(event) || Event.ONKEYPRESS == DOM.eventGetType(event)) {
            event.preventDefault();
            event.stopPropagation();
        } else if (Event.ONKEYUP == DOM.eventGetType(event)) {
            searchBox.onKeyUp(event.getKeyCode(), event.getShiftKey());
            event.preventDefault();
            event.stopPropagation();
        }
    }

    public void cleanup() {
        searchBox = null;
    }
}
