package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkCommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory.OperationMap;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.form.DnDPanel;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public abstract class NetworkItemPanel extends DnDPanel {

    public static final String SETUP_NETWORKS_DATA = "SetupNetworksData"; //$NON-NLS-1$
    public static final String SETUP_NETWORKS_TYPE = "SetupNetworksType"; //$NON-NLS-1$
    final ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();
    final ApplicationMessages messages = ClientGinjectorProvider.instance().getApplicationMessages();
    final Image dragImage = new Image(resources.itemDraggable());
    final PushButton actionButton;
    final NetworkItemModel<?> item;

    final protected NetworkPanelsStyle style;
    protected NetworkItemPanel parentPanel;
    private MenuBar menu;

    // statics
    private static final PopupPanel menuPopup = new PopupPanel(true);

    private static final ItemInfoPopup infoPopup = new ItemInfoPopup();

    public NetworkItemPanel(NetworkItemModel<?> item, NetworkPanelsStyle style, Boolean draggable) {
        super(draggable);
        dragImage.setVisible(false);
        Image editImage = new Image(resources.editHover());
        actionButton = new PushButton(editImage, new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                actionButton.setVisible(false);
                NetworkItemPanel.this.onAction();
            }
        });
        actionButton.getDownFace().setImage(new Image(resources.editMouseDown()));
        actionButton.setPixelSize(editImage.getWidth(), editImage.getHeight());
        actionButton.setVisible(false);

        this.item = item;
        this.style = style;
        init();

        addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                NetworkItemPanel.this.onMouseOver();
            }
        });

        addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                NetworkItemPanel.this.onMouseOut();
            }
        });
    }

    protected abstract Widget getContents();

    protected abstract void onAction();

    protected void onMouseOut() {
        dragImage.setVisible(false);
        infoPopup.hide(true);
        // handle nested panels (for example bonded nics) so nic.mouseOut() should cause parent.mouseIn()
        if (parentPanel != null) {
            parentPanel.onMouseOver();
        }
    }

    protected void onMouseOver() {
        dragImage.setVisible(true);
        infoPopup.showItem(item, this);
    }

    private void executeCommand(NetworkOperation operation, NetworkCommand command) {
        menuPopup.hide();
        NetworkItemPanel.this.item.getSetupModel().onOperation(operation, command);
    }

    private void init() {
        menu = menuFor(item);
        getElement().addClassName(style.itemPanel());

        setWidget(getContents());

        addDomHandler(new ContextMenuHandler() {

            @Override
            public void onContextMenu(ContextMenuEvent event) {
                NetworkItemPanel sourcePanel = (NetworkItemPanel) event.getSource();
                NativeEvent nativeEvent = event.getNativeEvent();
                showContextMenu(sourcePanel, nativeEvent.getClientX(), nativeEvent.getClientY());
                event.stopPropagation();
                event.preventDefault();
            }

        }, ContextMenuEvent.getType());

        // drag start
        addBitlessDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                NetworkItemPanel sourcePanel = (NetworkItemPanel) event.getSource();
                // Required: set data for the event.
                event.setData("Text", sourcePanel.item.getType() + " " + sourcePanel.item.getName()); //$NON-NLS-1$ //$NON-NLS-2$

                // show a ghost of the widget under cursor.
                NativeEvent nativeEvent = event.getNativeEvent();
                int x = nativeEvent.getClientX() - sourcePanel.getAbsoluteLeft();
                int y = nativeEvent.getClientY() - sourcePanel.getAbsoluteTop();
                event.getDataTransfer().setDragImage(sourcePanel.getElement(), x, y);
            }
        }, DragStartEvent.getType());

    }

    /**
     * Generate a Menu for the provided Network Item.
     *
     * @param item
     * @return
     */
    private MenuBar menuFor(NetworkItemModel<?> item) {
        MenuBar menu = rootMenu(item);
        OperationMap operationMap = item.getSetupModel().commandsFor(item);
        for (final NetworkOperation operation : operationMap.keySet()) {
            final List<NetworkCommand> commands = operationMap.get(operation);
            if (operation.isUnary()) {
                assert commands.size() == 1 : "Got a NetworkCommand List with more than one Unary Operation"; //$NON-NLS-1$
                menu.addItem(operation.getVerb(item), new Command() {
                    @Override
                    public void execute() {
                        executeCommand(operation, commands.get(0));
                    }
                });
            } else {
                Collections.sort(commands, new Comparator<NetworkCommand>() {
                    @Override
                    public int compare(NetworkCommand o1, NetworkCommand o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                MenuBar subMenu = subMenu();
                for (final NetworkCommand command : commands) {
                    subMenu.addItem(new MenuItem(command.getName(), new Command() {
                        @Override
                        public void execute() {
                            executeCommand(operation, command);
                        }
                    }));
                }
                menu.addItem(operation.getVerb(item), subMenu);
            }
        }
        return menu;
    }

    private MenuBar rootMenu(NetworkItemModel<?> item) {
        MenuBar menuBar = new MenuBar(true);
        menuBar.addItem("Menu for " + item.getName(), (Command) null); //$NON-NLS-1$
        menuBar.addSeparator();
        return menuBar;
    }

    private void showContextMenu(NetworkItemPanel panel, int clientX, int clientY) {
        menuPopup.setWidget(menu);
        menuPopup.setPopupPosition(clientX, clientY);
        menuPopup.show();
    }

    private MenuBar subMenu() {
        MenuBar menuBar = new MenuBar(true);
        return menuBar;
    }

    public NetworkItemModel<?> getItem() {
        return item;
    }

    public static String getData(String dragDropEventData) {
        if (dragDropEventData == null) {
            return ""; //$NON-NLS-1$
        }
        int split = dragDropEventData.indexOf(" "); //$NON-NLS-1$
        if (split == -1 || dragDropEventData.length() == split + 1) {
            return ""; //$NON-NLS-1$
        }
        return dragDropEventData.substring(split + 1);
    }

    public static String getType(String dragDropEventData) {
        if (dragDropEventData == null) {
            return ""; //$NON-NLS-1$
        }
        int split = dragDropEventData.indexOf(" "); //$NON-NLS-1$
        if (split == -1 || split == 0) {
            return ""; //$NON-NLS-1$
        }
        return dragDropEventData.substring(0, split);
    }
}
