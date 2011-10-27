package org.ovirt.engine.ui.userportal.client.views.basic.components;

import com.google.gwt.core.client.GWT;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommon.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.userportal.client.common.Severity;
import org.ovirt.engine.ui.userportal.client.components.ChangeableEdgeVLayout;
import org.ovirt.engine.ui.userportal.client.components.GridElement;
import org.ovirt.engine.ui.userportal.client.components.UPLabel;
import org.ovirt.engine.ui.userportal.client.modalpanels.MessageDialog;
import org.ovirt.engine.ui.userportal.client.protocols.ConsoleUtils;
import org.ovirt.engine.ui.userportal.client.protocols.Protocol;
import org.ovirt.engine.ui.userportal.client.protocols.ProtocolOptionContainer;
import org.ovirt.engine.ui.userportal.client.uicommonext.RDPInterfaceImpl;
import org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class VmTvLayout extends VLayout implements GridElement<UserPortalItemModel>, ProtocolOptionContainer {
	private ChangeableEdgeVLayout mainLayout;
	private UPLabel vmNameLabel;
	private Img vmImage;
	private UPLabel vmStatusLabel;
	private Guid entityGuid;
	private ActionButton runButton;
	private ActionButton shutdownButton;
	private ActionButton suspendButton;
	private UPLabel vmImageConsoleBanner;
	private UserPortalItemModel currentItem;
	private Img vmImageDisabledMask;
	private Img vmImagePausedImage;
	private Protocol selectedProtocol;
	private static final int VM_IMAGE_WIDTH = 150;
	private static final int VM_IMAGE_HEIGHT = 120;
	private static final int VM_IMAGE_BANNER_HEIGHT = 33;
	private boolean isUp = true;
	private boolean isSelected = false;
	private String protocolMessage;
		
	public VmTvLayout(final UserPortalItemModel item) {	    
	    currentItem = item;
		entityGuid = item.getIsPool() ? ((vm_pools)item.getEntity()).getvm_pool_id() : ((VM)item.getEntity()).getvm_guid();
		setMembersMargin(3);

		String vmID = UserPortalTools.getSafeId(entityGuid.toString());
		
		mainLayout = new ChangeableEdgeVLayout(150, 170, item.IsVmUp() ?  "edges/tvlayout" : "edges/tvlayout_down", "png", 8, "vmTvLayout_mainLayout_");		
		mainLayout.setMembersMargin(5);		

		vmImageConsoleBanner = new UPLabel("vmTvConsoleBanner");
		vmImageConsoleBanner.setHeight(VM_IMAGE_BANNER_HEIGHT);
		vmImageConsoleBanner.setWidth(VM_IMAGE_WIDTH);
		vmImageConsoleBanner.setBackgroundImage("general/tvlayout_doubleclick_to_console.png");
		vmImageConsoleBanner.setContents("Double Click for Console");
		vmImageConsoleBanner.setTop(VM_IMAGE_HEIGHT/2 - VM_IMAGE_BANNER_HEIGHT/2);
		vmImageConsoleBanner.setAlign(Alignment.CENTER);
		vmImageConsoleBanner.hide();		

		vmImage = new Img();
		vmImage.setWidth(VM_IMAGE_WIDTH);
		vmImage.setHeight(VM_IMAGE_HEIGHT);
		vmImage.setCanHover(true);
		vmImage.addChild(vmImageConsoleBanner);

		vmImageDisabledMask = new Img("general/tvlayout_disabled_mask.png", VM_IMAGE_WIDTH, VM_IMAGE_HEIGHT);
		vmImageDisabledMask.hide();
		vmImage.addChild(vmImageDisabledMask);

		vmImagePausedImage = new Img("status/pause_icon.png", 73,81);
		vmImagePausedImage.setTop(20);
		vmImagePausedImage.setLeft(39);
		vmImagePausedImage.hide();		
		vmImage.addChild(vmImagePausedImage);


		mainLayout.addMember(vmImage);

		vmStatusLabel = new UPLabel("vmTvStatusLabel");
		vmStatusLabel.setLayoutAlign(Alignment.CENTER);		
		mainLayout.addMember(vmStatusLabel);		

		HLayout actionButtons = new HLayout(5);
		actionButtons.setLayoutAlign(Alignment.CENTER);
		actionButtons.setAutoWidth();

		runButton = new ActionButton("actions/play.png", 24, 24, item.getIsPool() ? item.getTakeVmCommand() : item.getRunCommand(), item.getIsPool() ? "Take VM" : "Run VM");
		runButton.setID(getID() + "_runButton_" + vmID);
		actionButtons.addMember(runButton);		

		shutdownButton = new ActionButton("actions/stop.png", 24, 24, item.getShutdownCommand(), "Shutdown VM");
		shutdownButton.setID(getID() + "_shutdownButton_" + vmID);
		actionButtons.addMember(shutdownButton);		

		suspendButton = new ActionButton("actions/pause.png", 24, 24, item.getPauseCommand(), "Suspend VM");
		suspendButton.setID(getID() + "_suspendButton_" + vmID);
		actionButtons.addMember(suspendButton);		

		mainLayout.addMember(actionButtons);

		vmNameLabel = new UPLabel("vmTvNameLabel");	
		vmNameLabel.setLayoutAlign(Alignment.CENTER);

		ConsoleUtils.determineDefaultProtocol(this, item);
		
		updateValues(item);

		addMember(vmNameLabel);
		addMember(mainLayout);

		setVmImageConsoleEvent();
	}
	
	private void setVmImageConsoleEvent() {
		addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (!isSelected)
					mainLayout.setStyleName("vmTvLayoutOver");
				if (selectedProtocol != null) {
					if ((selectedProtocol.equals(Protocol.SPICE) && ConsoleUtils.canOpenSpiceConsole(currentItem)) || 
							(selectedProtocol.equals(Protocol.RDP) && ConsoleUtils.canOpenRDPConsole(currentItem))) {
						vmImage.setCursor(Cursor.HAND);
						vmImageConsoleBanner.setCursor(Cursor.HAND);
						vmImageConsoleBanner.show();
					}
				}
			}
		});
		
		addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				updateRowColor();
				if (vmImageConsoleBanner.isVisible()) {
					vmImage.setCursor(Cursor.DEFAULT);
					vmImageConsoleBanner.setCursor(Cursor.DEFAULT);
					vmImageConsoleBanner.hide();
				}
			}
		});
		
		vmImage.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				GWT.log("Executing connect to console command");
				if (selectedProtocol != null) {
					switch(selectedProtocol) {
					case SPICE:
						if (ConsoleUtils.canOpenSpiceConsole(currentItem)) {
							currentItem.getDefaultConsole().getConnectCommand().Execute();
						}
						else {
							if (!ConsoleUtils.isSpiceAvailable()) {
								Message message = new Message("Error Connecting to " + currentItem.getName(), "This browser does not support SPICE protocol");
								MessageDialog errorDialog = new MessageDialog("Not Supported", message, Severity.ERROR);
								errorDialog.draw();
							}
						}
						break;
					case RDP:
						if (ConsoleUtils.canOpenRDPConsole(currentItem)) {
							currentItem.getAdditionalConsole().getConnectCommand().Execute();
						}
						else {
							if (!ConsoleUtils.isRDPAvailable()) {
								Message message = new Message("Error Connecting to " + currentItem.getName(), "This browser does not support RDP protocol");
								MessageDialog errorDialog = new MessageDialog("Not Supported", message, Severity.ERROR);
								errorDialog.draw();
							}
						}
						break;
					}
				}
			}
		});		
	}

	public void updateValues(UserPortalItemModel item) {
		currentItem = item;
		setEntityStatus(item.getStatus());
		setEntityName(item.getName());
		
		if (item.getStatus().equals(VMStatus.Suspended) || item.getStatus().equals(VMStatus.Paused)) {
			if (!vmImagePausedImage.isVisible())
				vmImagePausedImage.show();
		}
		else {
			if (vmImagePausedImage.isVisible())
				vmImagePausedImage.hide();
		}

		updateRowColor();
		
		if (item.IsVmUp() != isUp) {
			if (item.IsVmUp()) {
				isUp = true;
				mainLayout.setEdgeImage("edges/tvlayout", "png");
				vmImageDisabledMask.hide();
			}
			else {
				isUp = false;
				mainLayout.setEdgeImage("edges/tvlayout_down", "png");
				vmImageDisabledMask.show();
			}
		}
		
		setEntityImage("os/large/" + item.getOsType().name() + ".png");
		updateActionsAvailability(item);
	}

	public void updateRowColor() {
		if (isSelected)
			mainLayout.setStyleName("vmTvLayoutSelected");
		else
			mainLayout.setStyleName(currentItem.IsVmUp() ? "vmTvLayoutRunning" : "vmTvLayout");

	}
	
	public void updateActionsAvailability(UserPortalItemModel item) {
		runButton.setDisabled(item.getIsPool() ? !item.getTakeVmCommand().getIsExecutionAllowed() : !item.getRunCommand().getIsExecutionAllowed());
		shutdownButton.setDisabled(!item.getShutdownCommand().getIsExecutionAllowed());
		suspendButton.setDisabled(!item.getPauseCommand().getIsExecutionAllowed());
	}

	public void setEntityName(String name) {
		if (!vmNameLabel.getContents().equals(name)) {
			GWT.log("Setting VM name to: " + name);
			vmNameLabel.setContents(name);
		}
	}
	public void setEntityStatus(VMStatus status) {
		String statusString = statusTranslator(status);
		
		if (!vmStatusLabel.getContents().equals(statusString)) {
			GWT.log("Setting status to: " + statusString);
			vmStatusLabel.setContents(statusString);
		}
	}
	public void setEntityImage(String imageName) {
		if (!vmImage.getSrc().equals(imageName)) {
			GWT.log("Setting image to: " + imageName);
			vmImage.setSrc(imageName);
		}
	}

	public void select() {
		isSelected = true;
		vmStatusLabel.setBaseStyle("vmTvStatusLabelSelected");
		mainLayout.setStyleName("vmTvLayoutSelected");
	}

	public void deselect() {
		isSelected = false;
		vmStatusLabel.setBaseStyle("vmTvStatusLabel");
		updateRowColor();
	}
	
	class ActionButton extends ImgButton {
		private String commandName;
		
		public ActionButton(String imgName, Integer width, Integer height, final UICommand command, String tooltipTitle) {
			super();
			setSize(width.toString(), height.toString());
			setSrc(imgName);
			//setShowDown(false);
			setShowRollOver(false);
			setTooltip(tooltipTitle);
			setHoverOpacity(75);
			setHoverStyle("gridToolTipStyle");
			setHoverWidth(1);
			setHoverWrap(false);
			setHoverDelay(500);
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
				}
			});
		}

		@Override
		public void setDisabled(boolean disabled) {
			if (getDisabled() != disabled)
				super.setDisabled(disabled);
		}
	}

	@Override
	public void setItemId(Object id) {
		entityGuid = (Guid)id;
	}

	@Override
	public Object getItemId() {
		return entityGuid;
	}
	
	public void setProtocol(Protocol protocol) {
		selectedProtocol = protocol;
	}
	
	public Protocol getProtocol() {
		return selectedProtocol;
	}

	public void setProtocolMessage(String message) {
		protocolMessage = message;
	}
	
	public String getProtocolMessage() {
		return protocolMessage;
	}
	
	public String statusTranslator(VMStatus status) {
		switch (status) {
		case WaitForLaunch:
		case PoweringUp:
		case RebootInProgress:
		case RestoringState:
			return "Powering Up";
		case MigratingFrom:
		case MigratingTo:
		case Up:
			return "Machine is Ready";
		case Paused:
		case Suspended:
			return "Paused";
		case PoweredDown:
		case PoweringDown:
			return "Powering Down";
		case Unknown:
		case Unassigned:
		case NotResponding:
		case ImageIllegal:
			return "Not Available";
		case SavingState:
		case ImageLocked:
			return "Please Wait..";
		case Down:
			return "Machine is Down";
		}
		return null;
	}
}
