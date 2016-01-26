package org.ovirt.engine.ui.webadmin.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.MenuPanelPopup;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuItem;

public class MenuCell<T> extends AbstractCell<T> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    private MenuPanelPopup menuPanelPopup;

    public MenuCell() {
        super();
        this.menuPanelPopup = new MenuPanelPopup(true);
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {
        if (!isVisible(value)) {
            return;
        }

        ImageResource image = resources.expanderDownImage();
        SafeHtml imageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(image).getHTML());
        sb.append(templates.volumeActivityMenu(imageHtml, id));
    }

    protected boolean isVisible(T value) {
        return true;
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            T value,
            SafeHtml tooltipValue,
            NativeEvent event,
            ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, tooltipValue, event, valueUpdater);

        int eventX = event.getClientX();
        int eventY = event.getClientY();

        // Handle the click event.
        if (BrowserEvents.CLICK.equals(event.getType())) {
            // Ignore clicks that occur outside of the outermost element.
            EventTarget eventTarget = event.getEventTarget();
            if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
                menuPanelPopup.asPopupPanel().showAndFitToScreen(eventX, eventY);
            }
        }
        else if(BrowserEvents.MOUSEOVER.equals(event.getType())) {
            if (isVisible(value)) {
                parent.getFirstChildElement().getStyle().setBorderColor("#96B7D6"); //$NON-NLS-1$
            }
        }
        else {
            parent.getFirstChildElement().getStyle().setBorderColor("transparent"); //$NON-NLS-1$
        }
    }

    public void addMenuItem(final ActionButtonDefinition<T> buttonDef) {
        final MenuItem menuItem = new MenuItem(buttonDef.getText(), new Command() {
            @Override
            public void execute() {
                menuPanelPopup.asPopupPanel().hide();
                buttonDef.onClick(null);
            }
        });
        menuItem.setEnabled(buttonDef.isEnabled(null));

        // Update button whenever its definition gets re-initialized
        buttonDef.addInitializeHandler(new InitializeHandler() {
            @Override
            public void onInitialize(InitializeEvent event) {
                menuItem.setEnabled(buttonDef.isEnabled(null));
            }
        });
        menuPanelPopup.getMenuBar().addItem(menuItem);
    }

}
