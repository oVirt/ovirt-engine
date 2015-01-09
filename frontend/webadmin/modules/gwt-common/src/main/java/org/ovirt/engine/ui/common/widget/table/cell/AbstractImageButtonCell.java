package org.ovirt.engine.ui.common.widget.table.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;

/**
 * Cell that renders ActionButtonDefinition-like image buttons.
 *
 * @param <T>
 *            The data type of the cell (the model)
 */
public abstract class AbstractImageButtonCell<T> extends AbstractCell<T> {

    private final SafeHtml imageHtml;

    // DOM element ID settings for the text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    public AbstractImageButtonCell(ImageResource image) {
        super(BrowserEvents.CLICK);
        this.imageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(image).getHTML());
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        if (BrowserEvents.CLICK.equals(event.getType())) {
            onClick(value);
        }
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<span id=\"" //$NON-NLS-1$
                + ElementIdUtils.createTableCellElementId(elementIdPrefix, columnId, context)
                + "\" style=\"vertical-align: middle;\" title=\"" //$NON-NLS-1$
                + SafeHtmlUtils.htmlEscape(getTitle(value))
                + "\">"); //$NON-NLS-1$
        sb.append(imageHtml);
        sb.appendHtmlConstant("</span>"); //$NON-NLS-1$
    }

    protected abstract String getTitle(T value);

    /**
     * Get the UICommand associated with the button.
     * @param value
     * @return
     */
    protected abstract UICommand resolveCommand(T value);

    /**
     * Execute the click command.
     * @param value
     */
    protected void onClick(T value) {
        UICommand command = resolveCommand(value);
        if (command != null) {
            command.execute();
        }
    }

}
