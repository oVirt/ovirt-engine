package org.ovirt.engine.ui.userportal.widget.extended.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.userportal.widget.action.UserPortalImageButtonDefinition;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Adapts the UserPortalImageButtonDefinition to a cell
 *
 * @param <T>
 *            The data type of the cell (the model)
 */
public abstract class ImageButtonCell<T> extends AbstractCell<T> {

    private String enabledCss;
    private String disabledCss;

    public ImageButtonCell(String enabledCss,
            String disabledCss) {
        super("click");
        this.enabledCss = enabledCss;
        this.disabledCss = disabledCss;
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            T value,
            NativeEvent event,
            ValueUpdater<T> valueUpdater
            ) {

        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        UserPortalImageButtonDefinition<T> command = createButtonDefinition(value);

        if ("click".equals(event.getType())) {

            if (!command.isEnabled(cast(value))) {
                return;
            }
            command.onClick(cast(value));
        }

        // TODO add CSS changing (the cursor to hand when crosses an enabled button)
    }

    @Override
    public void render(Context context, T data, SafeHtmlBuilder sb) {

        UserPortalImageButtonDefinition<T> buttonDefinition = createButtonDefinition(data);
        boolean isEnabled = buttonDefinition.isEnabled(cast(data));
        sb.appendHtmlConstant("<span class=\"" + (isEnabled ? enabledCss : disabledCss) + "\" title=\""
                + SafeHtmlUtils.htmlEscape(buttonDefinition.getTitle()) + "\">");
        if (isEnabled) {
            sb.append(buttonDefinition.getEnabledHtml());
        } else {
            sb.append(buttonDefinition.getDisabledHtml());
        }
        sb.appendHtmlConstant("</span>");
    }

    private List<T> cast(T t) {
        // not using Arrays.asList() because it would mean a warning
        List<T> ret = new ArrayList<T>();
        ret.add(t);
        return ret;
    }

    protected abstract UserPortalImageButtonDefinition<T> createButtonDefinition(T data);

}
