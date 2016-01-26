package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipConfig.Width;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkCommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public abstract class NetworkItemPanel<T extends NetworkItemModel<?>> extends FocusPanel {

    public static final String SETUP_NETWORKS_DATA = "SetupNetworksData"; //$NON-NLS-1$
    public static final String SETUP_NETWORKS_TYPE = "SetupNetworksType"; //$NON-NLS-1$

    private static final ApplicationResources resources = AssetProvider.getResources();

    final Image dragImage = new Image(resources.itemDraggable());
    final PushButton actionButton;
    final T item;

    private final boolean draggable;
    protected final NetworkPanelsStyle style;
    protected NetworkItemPanel<?> parentPanel;
    private MenuBar menu;

    private WidgetTooltip tooltip;

    // statics
    private static final PopupPanel menuPopup = new PopupPanel(true);

    private static final ItemInfoPopup infoPopup = new ItemInfoPopup();

    private static String lastDragData = ""; //$NON-NLS-1$

    public NetworkItemPanel(T item, NetworkPanelsStyle style, boolean draggable) {
        this.draggable = draggable;
        getElement().setDraggable(draggable ? Element.DRAGGABLE_TRUE : Element.DRAGGABLE_FALSE);

        dragImage.setVisible(false);
        Image editImage = new Image(resources.editHover());
        actionButton = new PushButton(editImage, new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dragImage.setVisible(false);
                NetworkItemPanel.this.onAction();
            }
        });
        actionButton.getDownFace().setImage(new Image(resources.editMouseDown()));
        actionButton.setPixelSize(editImage.getWidth(), editImage.getHeight());

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

        //
        // add mousedown handler for hiding the InfoItemPopup tooltip.
        // This is important because we have drag-and-drop targets in this dialog,
        // and on mousedown to initiate a drag, tooltips must be hidden
        // so they're not in the way of drop targets.

        addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                NetworkItemPanel.this.tooltip.hide();
            }
        });
    }

    protected abstract Widget getContents();

    protected abstract void onAction();

    protected void onMouseOut() {
        dragImage.setVisible(false);
        // handle nested panels (for example bonded nics) so nic.mouseOut() should cause parent.mouseIn()
        if (parentPanel != null) {
            parentPanel.onMouseOver();
        }
    }

    protected void onMouseOver() {
        dragImage.setVisible(draggable);
    }

    private void executeCommand(NetworkOperation operation, NetworkCommand command) {
        menuPopup.hide();
        NetworkItemPanel.this.item.getSetupModel().onOperation(operation, command);
    }

    private void init() {
        menu = menuFor(item);
        getElement().addClassName(style.itemPanel());

        initTooltip();
        setWidget(tooltip);

        addDomHandler(new ContextMenuHandler() {

            @Override
            public void onContextMenu(ContextMenuEvent event) {
                NetworkItemPanel<?> sourcePanel = (NetworkItemPanel<?>) event.getSource();
                NativeEvent nativeEvent = event.getNativeEvent();
                showContextMenu(sourcePanel, nativeEvent.getClientX(), nativeEvent.getClientY());
                event.stopPropagation();
                event.preventDefault();
            }

        }, ContextMenuEvent.getType());

        // drag start
        if (draggable) {
            addBitlessDomHandler(new DragStartHandler() {
                @Override
                public void onDragStart(DragStartEvent event) {
                    NetworkItemPanel<?> sourcePanel = (NetworkItemPanel<?>) event.getSource();
                    // Required: set data for the event.
                    lastDragData = sourcePanel.item.getType() + " " + sourcePanel.item.getName(); //$NON-NLS-1$
                    event.setData("Text", lastDragData); //$NON-NLS-1$

                    // show a ghost of the widget under cursor.
                    NativeEvent nativeEvent = event.getNativeEvent();
                    int x = nativeEvent.getClientX() - sourcePanel.getAbsoluteLeft();
                    int y = nativeEvent.getClientY() - sourcePanel.getAbsoluteTop();
                    event.getDataTransfer().setDragImage(sourcePanel.getElement(), x, y);
                }
            }, DragStartEvent.getType());
        }

    }

    protected void initTooltip() {
        tooltip = new WidgetTooltip(getContents());
        tooltip.setPlacement(Placement.BOTTOM);
        String tooltipContent = infoPopup.getTooltipContent(item, this);
        tooltip.setText(tooltipContent);
    }

    /**
     * Generate a Menu for the provided Network Item.
     */
    private MenuBar menuFor(NetworkItemModel<?> item) {
        MenuBar menu = rootMenu(item);
        Map<NetworkOperation, List<NetworkCommand>> operationMap = item.getSetupModel().commandsFor(item);
        for (final Entry<NetworkOperation, List<NetworkCommand>> entry : operationMap.entrySet()) {
            final List<NetworkCommand> commands = entry.getValue();
            if (entry.getKey().isUnary()) {
                assert commands.size() == 1 : "Got a NetworkCommand List with more than one Unary Operation"; //$NON-NLS-1$
                menu.addItem(entry.getKey().getVerb(item), new Command() {
                    @Override
                    public void execute() {
                        executeCommand(entry.getKey(), commands.get(0));
                    }
                });
            } else {
                Collections.sort(commands, new Comparator<NetworkCommand>() {
                    private LexoNumericComparator lexoNumeric = new LexoNumericComparator();

                    @Override
                    public int compare(NetworkCommand com1, NetworkCommand com2) {
                        return lexoNumeric.compare(com1.getName(), com2.getName());
                    }
                });
                MenuBar subMenu = subMenu();
                for (final NetworkCommand command : commands) {
                    subMenu.addItem(new MenuItem(command.getName(), new Command() {
                        @Override
                        public void execute() {
                            executeCommand(entry.getKey(), command);
                        }
                    }));
                }
                menu.addItem(entry.getKey().getVerb(item), subMenu);
            }
        }
        return menu;
    }

    private MenuBar rootMenu(NetworkItemModel<?> item) {
        MenuBar menuBar = new MenuBar(true);
        return menuBar;
    }

    private void showContextMenu(NetworkItemPanel<?> panel, int clientX, int clientY) {
        if (!menu.isEmpty()){
            menuPopup.setWidget(menu);
            menuPopup.setPopupPosition(clientX, clientY);
            menuPopup.show();
        }
    }

    private MenuBar subMenu() {
        MenuBar menuBar = new MenuBar(true);
        return menuBar;
    }

    public T getItem() {
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

    public static String getDragDropEventData(DragDropEventBase<?> event, boolean isDrop) {
        if (isDrop) {
            return event.getData("Text"); //$NON-NLS-1$
        } else {
            // On most of the browsers drag, dragenter, dragleave, dragover and dragend don't have access to event's
            // data
            return lastDragData;
        }
    }

    public void setToolTipMaxWidth(Width width){
        tooltip.setMaxWidth(width);
    }
}
