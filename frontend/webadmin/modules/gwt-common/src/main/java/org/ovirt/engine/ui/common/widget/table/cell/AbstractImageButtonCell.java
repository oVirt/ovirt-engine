package org.ovirt.engine.ui.common.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Cell that renders ActionButtonDefinition-like image buttons. Supports tooltips.
 *
 * @param <T>
 *            The data type of the cell (the model)
 */
public abstract class AbstractImageButtonCell<T> extends AbstractCell<T> {

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<span id=\"{0}\" class=\"{1}\">{2}</span>")
        SafeHtml span(String id, String classNames, SafeHtml html);
    }

    private static CellTemplate template = GWT.create(CellTemplate.class);

    private final SafeHtml enabledHtml;
    private final String enabledClassNames;

    private final SafeHtml disabledHtml;
    private final String disabledClassNames;

    public AbstractImageButtonCell(ImageResource enabledImage, String enabledClassNames,
            ImageResource disabledImage, String disabledClassNames) {
        super();
        this.enabledHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(enabledImage).getHTML());
        this.enabledClassNames = enabledClassNames;
        this.disabledHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(disabledImage).getHTML());
        this.disabledClassNames = disabledClassNames;
    }

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
    public void onBrowserEvent(Context context, Element parent, T value, SafeHtml tooltip, NativeEvent event, ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, tooltip, event, valueUpdater);

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

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {
        String css = isEnabled(value) ? enabledClassNames : disabledClassNames;
        SafeHtml html = isEnabled(value) ? enabledHtml : disabledHtml;

        sb.append(template.span(id, css, html));
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
