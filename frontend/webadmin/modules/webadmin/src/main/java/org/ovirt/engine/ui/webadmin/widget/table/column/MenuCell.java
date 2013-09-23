package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.action.MenuPanelPopup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuItem;

public class MenuCell<T> extends AbstractCell<T> {

    private ApplicationTemplates templates = ClientGinjectorProvider.getApplicationTemplates();

    private ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    private MenuPanelPopup menuPanelPopup;

    public MenuCell() {
        super("click"); //$NON-NLS-1$
        this.menuPanelPopup = new MenuPanelPopup(true);
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        ImageResource image = resources.triangleDown();
        SafeHtml imageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(image).getHTML());
        sb.append(templates.image(imageHtml));
    }

    public void onBrowserEvent(Context context,
            Element parent,
            T value,
            NativeEvent event,
            ValueUpdater<T> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        int eventX = event.getClientX();
        int eventY = event.getClientY();
        // Handle the click event.
        if ("click".equals(event.getType())) { //$NON-NLS-1$
            // Ignore clicks that occur outside of the outermost element.
            EventTarget eventTarget = event.getEventTarget();
            if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
                menuPanelPopup.asPopupPanel().showAndFitToScreen(eventX, eventY);
            }
        }
    }

    public void addMenuItem(String title, final UICommand command) {
        final MenuItem menuItem = new MenuItem(title, new Command() {
            @Override
            public void execute() {
                menuPanelPopup.asPopupPanel().hide();
                command.execute();
            }
        });
        menuItem.setEnabled(command.getIsExecutionAllowed());

        command.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs e = (PropertyChangedEventArgs) args;
                if (e.PropertyName.equals("IsExecutionAllowed")) { //$NON-NLS-1$
                    menuItem.setEnabled(command.getIsExecutionAllowed());
                }
            }
        });

        menuPanelPopup.getMenuBar().addItem(menuItem);
    }

}
