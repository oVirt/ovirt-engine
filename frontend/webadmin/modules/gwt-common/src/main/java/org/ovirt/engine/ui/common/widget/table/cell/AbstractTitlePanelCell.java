package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Label;
import org.ovirt.engine.ui.common.widget.ElementAwareDecoratedPopupPanel;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTitlePanelCell<T> extends AbstractCell<T> {

    private String title = ""; //$NON-NLS-1$
    private ElementAwareDecoratedPopupPanel titlePanel = new ElementAwareDecoratedPopupPanel();

    private Set<String> consumedEvents;

    @Override
    public Set<String> getConsumedEvents() {
        return consumedEvents;
    }

    public AbstractTitlePanelCell() {
        super();

        consumedEvents = new HashSet<String>();
        consumedEvents.add(BrowserEvents.MOUSEOVER);
        consumedEvents.add(BrowserEvents.MOUSEOUT);
    }

    /**
     * Set the text for the tooltip that will show when this cell is hovered over.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title != null ? title : ""; //$NON-NLS-1$
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
        String eventType = event.getType();
        handleTitlePanel(eventType, parent);
    }

    private void handleTitlePanel(String eventType, Element parent) {
        if (title.isEmpty()) {
            // no need to show/hide title
            return;
        }

        titlePanel.setWidget(new Label(title));
        if (BrowserEvents.MOUSEOVER.equals(eventType)) {
            titlePanel.showRelativeTo(parent);
        }
        else if (BrowserEvents.MOUSEOUT.equals(eventType)) {
            titlePanel.hide();
        }
    }

}
