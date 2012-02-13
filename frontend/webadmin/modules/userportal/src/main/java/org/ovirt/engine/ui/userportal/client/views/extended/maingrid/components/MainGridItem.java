package org.ovirt.engine.ui.userportal.client.views.extended.maingrid.components;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.ConsoleErrors;
import org.ovirt.engine.ui.frontend.ErrorTranslator;
import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommon.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommon.models.vms.ErrorCodeEventArgs;
import org.ovirt.engine.ui.uicommon.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.userportal.client.common.Severity;
import org.ovirt.engine.ui.userportal.client.components.GridElement;
import org.ovirt.engine.ui.userportal.client.components.UPLabel;
import org.ovirt.engine.ui.userportal.client.modalpanels.ConsoleEditPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.MessageDialog;
import org.ovirt.engine.ui.userportal.client.protocols.ConsoleUtils;
import org.ovirt.engine.ui.userportal.client.protocols.Protocol;
import org.ovirt.engine.ui.userportal.client.protocols.ProtocolOptionContainer;
import org.ovirt.engine.ui.userportal.client.uicommonext.RDPInterfaceImpl;
import org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.UserPortalItemsGrid;
import com.smartgwt.client.types.ImageStyle;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;
import com.smartgwt.client.widgets.layout.HLayout;

public class MainGridItem extends HLayout implements GridElement<UserPortalItemModel>, ProtocolOptionContainer {

	public static final int MAIN_GRID_ITEM_HEIGHT = 50;
	
	private Img vmImage;
	private Img vmImageDisabledMask;
	private UPLabel nameLabel;
	private UPLabel descriptionLabel;
	private Img statusImage;
	private ActionButton runButton;
	private ActionButton shutdownButton;
	private ActionButton suspendButton;
	private ActionButton powerButton;
	private ImgButton consoleButton;
	private ImgButton consoleEditButton;
	
	private UserPortalItemModel currentItem;

	private MainGridItem mainGridItem = this;
	
	private Object entityGuid;
	
	private static final int VM_IMAGE_WIDTH = 51;
	private static final int VM_IMAGE_HEIGHT = 41;
	private static final int STATUS_IMAGE_WIDTH = 35;
	private static final int ACTION_BUTTON_SIZE = 24;

	private boolean isSelected = false;

	private Translator statusTranslator = EnumTranslator.Create(VMStatus.class);
	private Translator osTranslator = EnumTranslator.Create(VmOsType.class);
	
	private UserPortalItemsGrid parent;

	private Protocol selectedProtocol;
	private String protocolMessage;
	
	private String itemID;
	
	public MainGridItem(UserPortalItemsGrid parent, UserPortalItemModel item) {
		this.parent = parent;
		setHeight(MAIN_GRID_ITEM_HEIGHT);
		setWidth100();
		setMembersMargin(1);
		setCanHover(true);
		setCanFocus(true);
		setStyleName("mainGrid-row");
		setLayoutLeftMargin(7);
		
		entityGuid = item.getIsPool() ? ((vm_pools)item.getEntity()).getvm_pool_id() : ((VM)item.getEntity()).getId();
		itemID = UserPortalTools.getSafeId(entityGuid.toString());
		currentItem = item;
		
		vmImage = new Img();
		vmImage.setWidth(VM_IMAGE_WIDTH);
		vmImage.setHeight(VM_IMAGE_HEIGHT);
		vmImage.setLayoutAlign(VerticalAlignment.CENTER);		
		vmImage.setBorder("1px solid white");		
		vmImage.setHoverOpacity(75);
		vmImage.setHoverStyle("gridToolTipStyle");
		vmImage.setHoverWidth(1);
		vmImage.setHoverWrap(false);
		vmImage.setHoverDelay(500);
		
		vmImageDisabledMask = new Img("general/tvlayout_disabled_mask.png", VM_IMAGE_WIDTH-2, VM_IMAGE_HEIGHT-2);
		vmImageDisabledMask.hide();
		vmImage.addChild(vmImageDisabledMask);
		
		nameLabel = new UPLabel("mainGrid-VMname");
		nameLabel.setLayoutAlign(VerticalAlignment.CENTER);
		nameLabel.setAutoFit(true);
		
		descriptionLabel = new UPLabel("mainGrid-VMdescription");
		descriptionLabel.setLayoutAlign(VerticalAlignment.CENTER);
		descriptionLabel.setAutoFit(true);
		
		statusImage = new Img();
		statusImage.setImageType(ImageStyle.CENTER);
		statusImage.setWidth(STATUS_IMAGE_WIDTH);
		statusImage.setHoverOpacity(75);
		statusImage.setHoverStyle("gridToolTipStyle");
		statusImage.setHoverWidth(1);
		statusImage.setHoverWrap(false);
		statusImage.setHoverDelay(500);

		HLayout actionButtonsLayout = new HLayout(3);
		actionButtonsLayout.setShowEdges(true);
		actionButtonsLayout.setEdgeSize(8);
		actionButtonsLayout.setEdgeImage("edges/buttonslayout.png");
		actionButtonsLayout.setHeight(40);
		actionButtonsLayout.setAutoWidth();
		actionButtonsLayout.setLayoutAlign(VerticalAlignment.CENTER);
		actionButtonsLayout.setStyleName("mainGrid-actionButtons");
		
		runButton = new ActionButton("actions/play.png", item.getIsPool() ? item.getTakeVmCommand() : item.getRunCommand(), (item.getIsPool() ? "takevm" : "run") + "_button_" + itemID);
		shutdownButton = new ActionButton("actions/stop.png", item.getIsPool() ? item.getReturnVmCommand() : item.getShutdownCommand(), (item.getIsPool() ? "returnvm" : "shutdown") + "_button_" + itemID);
		suspendButton = new ActionButton("actions/pause.png", item.getPauseCommand(), "pause_button_" + itemID);
		powerButton = new ActionButton("actions/power.png", item.getStopCommand(), "stop_button_" + itemID);
		actionButtonsLayout.setMembers(runButton, shutdownButton, suspendButton, powerButton);

		HLayout nameAndDescriptionLayout = new HLayout(5);
		nameAndDescriptionLayout.setMembers(nameLabel, descriptionLabel);
		nameAndDescriptionLayout.setWidth(400);
		nameAndDescriptionLayout.setOverflow(Overflow.HIDDEN);
		
		addMember(vmImage);
		addMember(statusImage);
		addMember(nameAndDescriptionLayout);
		addMember(actionButtonsLayout);
		
		consoleButton = new ImgButton();
		consoleButton.setSrc("actions/console.png");
		consoleButton.setWidth(80);
		consoleButton.setHeight(27);
		consoleButton.setLayoutAlign(VerticalAlignment.CENTER);
		consoleButton.setShowDown(false);
		consoleButton.addClickHandler(new ConsoleClickHandler());
		consoleButton.setHoverOpacity(75);
		consoleButton.setHoverStyle("gridToolTipStyle");
		consoleButton.setHoverWidth(1);
		consoleButton.setHoverWrap(false);
		consoleButton.setHoverDelay(500);		
    	consoleButton.setTooltip("Open Console");
    	consoleButton.setID("openconsole_button_" + itemID);
    	
		consoleEditButton = new ImgButton();
		consoleEditButton.setSrc("actions/console_edit.png");
		consoleEditButton.setWidth(15);
		consoleEditButton.setHeight(27);
		consoleEditButton.setLayoutAlign(VerticalAlignment.CENTER);
		consoleEditButton.setShowDown(false);
		consoleEditButton.addClickHandler(new ConsoleEditClickHandler());
		consoleEditButton.setHoverOpacity(75);
		consoleEditButton.setHoverStyle("gridToolTipStyle");
		consoleEditButton.setHoverWidth(1);
		consoleEditButton.setHoverWrap(false);
		consoleEditButton.setHoverDelay(500);
		consoleEditButton.setTooltip("Edit Console Options");
    	consoleEditButton.setID("editconsole_button_" + itemID);

		ConsoleUtils.determineDefaultProtocol(this, item);
		
		if (item.getIsPool()) {
    		consoleButton.setShowHover(false);
    		consoleEditButton.setShowHover(false);
			consoleButton.setDisabled(true);
			consoleEditButton.setDisabled(true);
		}
		
		addMember(consoleButton);
		addMember(consoleEditButton);
		
		updateValues(item);

		addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (!isSelected)
					setStyleName("mainGrid-rowOver");
			}
		});
		addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				updateRowColor();
			}
		});	
	}
	
	@Override
	public void updateValues(UserPortalItemModel item) {
		currentItem = item;
		statusImage.setTooltip(statusTranslator.get(item.getStatus()));
		setImage(item);
		setName(item.getName());
		setDescription(item.getDescription());
		setStatus(item.getStatus());
		updateRowColor();
		setVmImageMaskVisibility(item);
		updateActionsAvailability(item);
		updateConsoleOptions(item);
	}
	
	public void updateActionsAvailability(UserPortalItemModel item) {
		runButton.setDisabled(item.getIsPool() ? !item.getTakeVmCommand().getIsExecutionAllowed() : !item.getRunCommand().getIsExecutionAllowed());
		shutdownButton.setDisabled(!item.getShutdownCommand().getIsExecutionAllowed());
		suspendButton.setDisabled(!item.getPauseCommand().getIsExecutionAllowed());
		powerButton.setDisabled(!item.getStopCommand().getIsExecutionAllowed());
	}
	
	public void updateConsoleOptions(UserPortalItemModel item) {		
		if (selectedProtocol != null) {
			switch(selectedProtocol) {
			case SPICE:
				if (item.getDefaultConsole().getConnectCommand().getIsAvailable() && item.getDefaultConsole().getConnectCommand().getIsExecutionAllowed()) {
					enableConsoleButtons();
				}
				else {
					disableConsoleButtons();
				}
				break;
			case RDP:
				if (item.getAdditionalConsole().getConnectCommand().getIsAvailable() && item.getAdditionalConsole().getConnectCommand().getIsExecutionAllowed()) {
					enableConsoleButtons();
				}
				else {
					disableConsoleButtons();
				}
				break;
			}
		}
		else {
			disableConsoleButtons();
		}
	}

	public void setVmImageMaskVisibility(UserPortalItemModel item) {
		if (item.IsVmUp()) {
			vmImageDisabledMask.hide();
		}
		else {
			vmImageDisabledMask.show();
		}
	}
	
	public void updateRowColor() {
		if (isSelected) {
			setStyleName("mainGrid-rowSelected");
		}
		else {
			setStyleName(currentItem.IsVmUp() ? "mainGrid-rowRunning" : "mainGrid-row");
		}
	}
	
	public void setImage(UserPortalItemModel item) {
		String imageName = "os/large/" + item.getOsType().name() + ".png";
		if (!vmImage.getSrc().equals(imageName)) {
			vmImage.setTooltip(osTranslator.get(item.getOsType()));
			vmImage.setSrc(imageName);
		}
	}
	public void setName(String name) {
		nameLabel.setContents(name);
	}
	public void setDescription(String description) {
		if ((description == null || description.isEmpty())) {
			descriptionLabel.setContents(null, true);
			return;
		}

		String descriptionString = '(' + description + ')';
		descriptionLabel.setContents(descriptionString, true);	
	}
	public void setStatus(VMStatus status) {
		StatusIcon statusIcon = STATUS_ICON_MAP.get(status);
		String statusImageName = "status/" +  statusIcon + ".png";
		if (!statusImage.getSrc().equals(statusImageName)) {			
			statusImage.setSrc(statusImageName);
		}
	}
	
	@Override
	public void select() {
		isSelected = true;
		setStyleName("mainGrid-rowSelected");
		focus();
	}

	@Override
	public void deselect() {
		isSelected = false;
		updateRowColor();
	}

	@Override
	public void setItemId(Object id) {
		entityGuid = id;
	}

	@Override
	public Object getItemId() {
		return entityGuid;
	}

	enum StatusIcon {
		PAUSED, RUNNING, STOPPED, WAITING, QUESTIONMARK, STOPPING, STARTING;
	}
	
	// A static HashMap that serves as a conversion table between the VM status
	// and the corresponding icon
	final static Map<VMStatus, StatusIcon> STATUS_ICON_MAP = new HashMap<VMStatus, StatusIcon>() {
		private static final long serialVersionUID = -1L;
		{
			put(VMStatus.Up, StatusIcon.RUNNING);
			put(VMStatus.WaitForLaunch, StatusIcon.WAITING);
			put(VMStatus.PoweringUp, StatusIcon.STARTING);
			put(VMStatus.Paused, StatusIcon.PAUSED);
			put(VMStatus.Suspended, StatusIcon.PAUSED);
			put(VMStatus.ImageLocked, StatusIcon.WAITING);
			put(VMStatus.Unknown, StatusIcon.QUESTIONMARK);
			put(VMStatus.MigratingFrom, StatusIcon.WAITING);
			put(VMStatus.MigratingTo, StatusIcon.WAITING);
			put(VMStatus.RebootInProgress, StatusIcon.STARTING);
			put(VMStatus.Unassigned, StatusIcon.STOPPED);
			put(VMStatus.ImageIllegal, StatusIcon.STOPPED);
			put(VMStatus.Down, StatusIcon.STOPPED);
			put(VMStatus.SavingState, StatusIcon.WAITING);
			put(VMStatus.RestoringState, StatusIcon.STARTING);
			put(VMStatus.PoweringDown, StatusIcon.STOPPING);
			put(VMStatus.PoweredDown, StatusIcon.STOPPED);
			put(VMStatus.NotResponding, StatusIcon.STOPPED);
		}
	};

	class ActionButton extends ImgButton {
		private String commandName;
		
		public ActionButton(String imgName, final UICommand command, String buttonID) {
			super();
			setSize(ACTION_BUTTON_SIZE);
			setSrc(imgName);
			setShowRollOver(false);
			setTooltip(command.getName());
			setHoverOpacity(75);
			setHoverStyle("gridToolTipStyle");
			setHoverWidth(1);
			setHoverWrap(false);
			setHoverDelay(500);
			setID(buttonID);
			commandName = command.getName();
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GWT.log("Executing " + commandName + "  command");
					for (UICommand uiCommand : currentItem.getCommands()) {
						if (uiCommand.getName().equals(commandName)) {
							uiCommand.Execute();
							break;
						}
					}
					parent.getMainGrid().gridActionPerformed();
				}
			});
		}

		@Override
		public void setDisabled(boolean disabled) {
			if (getDisabled() != disabled)
				super.setDisabled(disabled);
		}
	}

	private static ErrorTranslator consoleErrorsTranslator = 
	    new ErrorTranslator((ConsoleErrors)GWT.create(ConsoleErrors.class));
	
	class ConsoleEditClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			ConsoleEditPanel editPanel = new ConsoleEditPanel(currentItem, mainGridItem);
			editPanel.draw();
		}
	}

	class ConsoleClickHandler implements ClickHandler {	    
	    @Override
		public void onClick(ClickEvent event) {
			
		    boolean shouldConnectSpice = selectedProtocol.equals(Protocol.SPICE) && 
		        (currentItem.getDefaultConsole() instanceof SpiceConsoleModel);
		    
		    boolean shouldConnectRDP = selectedProtocol.equals(Protocol.RDP) || 
                (currentItem.getHasAdditionalConsole() && currentItem.getAdditionalConsole().getConnectCommand().getIsAvailable());
		    
		    if (shouldConnectSpice) {
				if (!SpiceInterfaceImpl.isBrowserSupported()) {
				    showConnectErrorMessage(Protocol.SPICE);
				} else {
				    connectConsole(currentItem.getDefaultConsole());
				}
			} else if (shouldConnectRDP) {
				if (!RDPInterfaceImpl.isBrowserSupported()) {
				    showConnectErrorMessage(Protocol.RDP);
				} else {
				    connectConsole(currentItem.getAdditionalConsole());
				}
			}
			parent.getMainGrid().gridActionPerformed();
		}
		
		public void showConnectErrorMessage(Protocol protocol) {
		    Message message = new Message("Error Connecting to " + currentItem.getName(), "This browser does not support " + protocol.name() + " protocol");
            MessageDialog errorDialog = new MessageDialog("Not Supported", message, Severity.ERROR);
            errorDialog.draw();
		}
		
		public void showDisconnectErrorMessage(String error) {
            Message message = new Message("Virtual Machine connection error: " + error);
            MessageDialog errorDialog = new MessageDialog("Console Disconnected", message, Severity.ERROR);
            errorDialog.draw();
        }
		
		public void connectConsole(final ConsoleModel consoleModel) {		    
		    consoleModel.getErrorEvent().addListener(new IEventListener() {
	            @Override
	            public void eventRaised(Event ev, Object sender, EventArgs args) {
	                
	                // Get error code
	                ErrorCodeEventArgs errorCodeEventArgs = (ErrorCodeEventArgs)args;
	                int errorCode = errorCodeEventArgs.getErrorCode();
	                
	                // Translate error using translator and show error dialog
	                showDisconnectErrorMessage(consoleErrorsTranslator.TranslateErrorTextSingle("E" + errorCode));
	                
	                consoleModel.getErrorEvent().removeListener(this);
	            }
	        });
		    
		    consoleModel.getConnectCommand().Execute();
		}
	}
	
	private void disableConsoleButtons() {
		consoleButton.setDisabled(true);
		consoleEditButton.setDisabled(true);		
		consoleEditButton.setShowHover(false);

		if (getProtocolMessage() == null) {
			consoleButton.setShowHover(false);
		}
		else {
			consoleButton.setShowHover(true);
			consoleButton.setTooltip(getProtocolMessage());
		}
	}
	
	private void enableConsoleButtons() {
		consoleButton.setShowHover(true);
		consoleEditButton.setShowHover(true);
		consoleButton.setDisabled(false);
		consoleEditButton.setDisabled(false);
	}

	
	@Override
	public void setProtocol(Protocol protocol) {
		this.selectedProtocol = protocol;
	}
	
	@Override
	public Protocol getProtocol() {
		return selectedProtocol;
	}

	@Override
	public void setProtocolMessage(String message) {
		this.protocolMessage = message;
	}

	@Override
	public String getProtocolMessage() {
		return protocolMessage;
	}
}