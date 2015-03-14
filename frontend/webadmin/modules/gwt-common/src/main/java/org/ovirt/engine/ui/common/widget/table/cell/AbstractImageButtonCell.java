package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Cell that renders ActionButtonDefinition-like image buttons.
 *
 * @param <C>
 *            The data type of the cell (the model)
 */
public abstract class AbstractImageButtonCell<C> extends AbstractCell<C> {

    private final SafeHtml imageHtml;

    public AbstractImageButtonCell(ImageResource image) {
        super();
        this.imageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(image).getHTML());
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, C value, SafeHtml tooltipContent, NativeEvent event, ValueUpdater<C> valueUpdater) {
        super.onBrowserEvent(context, parent, value, tooltipContent, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        if (BrowserEvents.CLICK.equals(event.getType())) {
            onClick(value);
        }
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb, String id) {
        sb.appendHtmlConstant("<span id=\"" //$NON-NLS-1$
                + id
                + "\" style=\"vertical-align: middle;\">"); //$NON-NLS-1$
        sb.append(imageHtml);
        sb.appendHtmlConstant("</span>"); //$NON-NLS-1$
    }

    /**
     * Get the UICommand associated with the button.
     * @param value
     * @return
     */
    protected abstract UICommand resolveCommand(C value);

    /**
     * Execute the click command.
     * @param value
     */
    protected void onClick(C value) {
        UICommand command = resolveCommand(value);
        if (command != null) {
            command.execute();
        }
    }

}
