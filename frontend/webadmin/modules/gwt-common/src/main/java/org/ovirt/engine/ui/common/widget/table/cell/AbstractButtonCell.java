package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;

/**
 * Cell that renders ActionButtonDefinition-like buttons. Supports tooltips.
 *
 * @param <T>
 *            The data type of the cell (the model)
 */
public abstract class AbstractButtonCell<T> extends AbstractCell<T> {

    /**
     * Events to sink.
     */
    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        if (BrowserEvents.CLICK.equals(event.getType()) && isEnabled(value)) {
            UICommand command = resolveCommand(value);
            if (command != null) {
                command.execute();
            }
        }
    }

    /**
     * Check if the button is enabled.
     */
    protected boolean isEnabled(T value) {
        UICommand command = resolveCommand(value);
        return command != null ? command.getIsExecutionAllowed() : true;
    }

    protected abstract UICommand resolveCommand(T value);

}
