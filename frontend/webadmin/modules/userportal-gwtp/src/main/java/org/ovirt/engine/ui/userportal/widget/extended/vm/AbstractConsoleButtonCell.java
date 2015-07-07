package org.ovirt.engine.ui.userportal.widget.extended.vm;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * AbstractConsoleButtonCell. Supports tooltips.
 *
 */
public abstract class AbstractConsoleButtonCell extends AbstractCell<UserPortalItemModel> {

    public static interface ConsoleButtonCommand {
        public void execute(UserPortalItemModel model);
    }

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div id=\"{0}\" class=\"{1}\" data-class=\"consoleButton\" />")
        SafeHtml consoleButton(String id, String className);
    }

    private final CellTemplate template = GWT.create(CellTemplate.class);

    private final ConsoleButtonCommand command;

    private final String enabledCss;

    private final String disabledCss;

    public AbstractConsoleButtonCell(String enabledCss, String disabledCss, ConsoleButtonCommand command) {
        super();
        this.enabledCss = SafeHtmlUtils.htmlEscape(enabledCss);
        this.disabledCss = SafeHtmlUtils.htmlEscape(disabledCss);
        this.command = command;
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
    public void onBrowserEvent(Context context, Element parent,
            final UserPortalItemModel model, SafeHtml tooltip, NativeEvent event,
            ValueUpdater<UserPortalItemModel> valueUpdater) {
        super.onBrowserEvent(context, parent, model, tooltip, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();

        // Skip events other than 'click'
        if (!BrowserEvents.CLICK.equals(event.getType())) {
            return;
        }

        // Skip events that don't target consoleButton DIV element
        if (!DivElement.is(eventTarget) || !"consoleButton".equals(DivElement.as(eventTarget).getAttribute("data-class"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (shouldRenderCell(model)) {
            // deferred because first the row has to be selected and then the console can be shown
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    command.execute(model);
                }
            });
        }
    }

    @Override
    public void render(Context context, UserPortalItemModel model, SafeHtmlBuilder sb, String id) {
        sb.append(template.consoleButton(
                id,
                shouldRenderCell(model) ? enabledCss : disabledCss));
    }

    protected abstract boolean shouldRenderCell(UserPortalItemModel model);

}
