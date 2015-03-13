package org.ovirt.engine.ui.common.widget.table.cell;

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
 * @param <T>
 *            The data type of the cell (the model)
 */
public abstract class ImageButtonCell<T> extends AbstractCell<T> {

    private final SafeHtml enabledHtml;
    private final String enabledCss;

    private final SafeHtml disabledHtml;
    private final String disabledCss;

    public ImageButtonCell(ImageResource enabledImage, String enabledCss,
            ImageResource disabledImage, String disabledCss) {
        super(BrowserEvents.CLICK);
        this.enabledHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage).getHTML());
        this.enabledCss = enabledCss;
        this.disabledHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage).getHTML());
        this.disabledCss = disabledCss;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        if (BrowserEvents.CLICK.equals(event.getType()) && isEnabled(value)) {
            onClick(value);
        }
        // TODO change the image while the mouse is down (simulate click)
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {
        boolean isEnabled = isEnabled(value);
        // TODO(vszocs) consider using SafeHtmlTemplates instead of building HTML manually
        sb.appendHtmlConstant("<span id=\"" //$NON-NLS-1$
                + id
                + "\" class=\"" //$NON-NLS-1$
                + (isEnabled ? enabledCss : disabledCss)
                + "\" title=\"" //$NON-NLS-1$
                + SafeHtmlUtils.htmlEscape(getTitle(value))
                + "\">"); //$NON-NLS-1$
        if (isEnabled) {
            sb.append(enabledHtml);
        } else {
            sb.append(disabledHtml);
        }
        sb.appendHtmlConstant("</span>"); //$NON-NLS-1$
    }

    /**
     *
     * @param value
     * @return
     */
    protected abstract String getTitle(T value);

    /**
     * Get the UICommand associated with the button.
     * @param value
     * @return
     */
    protected abstract UICommand resolveCommand(T value);

    /**
     * Check if the button is enabled.
     * @param value
     * @return
     */
    protected boolean isEnabled(T value) {
        UICommand command = resolveCommand(value);
        return command != null ? command.getIsExecutionAllowed() : false;
    }

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
