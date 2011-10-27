package org.ovirt.engine.ui.userportal.client.views.basic;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.client.components.UPLabel;
import org.ovirt.engine.ui.userportal.client.modalpanels.ConsoleEditPanel;
import org.ovirt.engine.ui.userportal.client.protocols.Protocol;
import org.ovirt.engine.ui.userportal.client.protocols.ProtocolOptionContainer;
import org.ovirt.engine.ui.userportal.client.views.basic.components.VmTvLayout;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.Visibility;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CanvasItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class VmBasicInfoLayout extends VLayout implements ProtocolOptionContainer {
	private UPLabel vmNameLabel; 
	private UPLabel vmDescriptionLabel;
	private Img osImage;
	private StyledStaticTextItem osText;
	private StyledStaticTextItem memoryText;
	private StyledStaticTextItem cpuCoresText;
	private VLayout disksLayout = new VLayout();
	private VmBasicInfoLayout vmBasicInfoLayout = this;
	private UserPortalItemModel currentItem;
	private ProtocolOptionContainer currentItemLayout;
	private UPLabel consoleMessage;
	private StyledStaticTextItem consoleText;
	private StyledStaticTextItem consoleEditText;
	boolean selectionInit = false;
	Img vmTypeImage;
	
	private static final int OS_TYPE_IMAGE_SIZE = 50;
	
	private UserPortalBasicListModel model;
	
	public VmBasicInfoLayout(final UserPortalBasicListModel model) {
		this.model = model;
		setMinWidth(100);
		setPadding(10);
		setStyleName("basicViewInfoLayout");
		setOverflow(Overflow.AUTO);
		
		HLayout generalInfoLayout = new HLayout(10);
		generalInfoLayout.setWidth100();
		generalInfoLayout.setShowEdges(true);
		generalInfoLayout.setEdgeImage("edges/detailpanel.png");
		generalInfoLayout.setEdgeSize(7);
		generalInfoLayout.setEdgeBackgroundColor("white");
		generalInfoLayout.setHeight(78);
		generalInfoLayout.setWidth100();
		
		osImage = new Img();
		osImage.setSize(OS_TYPE_IMAGE_SIZE);
		osImage.setVisibility(Visibility.HIDDEN);
		osImage.setPadding(5);
		
		vmTypeImage = new Img();
		vmTypeImage.setLayoutAlign(VerticalAlignment.BOTTOM);
		osImage.addChild(vmTypeImage);
		generalInfoLayout.addMember(osImage);
		
		vmNameLabel = new UPLabel();
		vmNameLabel.setStyleName("basicInfoVmNameLabel");
	
		vmDescriptionLabel = new UPLabel();
		vmDescriptionLabel.setStyleName("basicInfoVmDescriptionLabel");
		vmDescriptionLabel.setWrap(true);
		vmDescriptionLabel.setWidth100();
		
		VLayout nameDescLayout = new VLayout();
		nameDescLayout.setLayoutAlign(Alignment.CENTER);
		nameDescLayout.addMember(vmNameLabel);
		nameDescLayout.addMember(vmDescriptionLabel);
		nameDescLayout.setAutoHeight();

		generalInfoLayout.addMember(nameDescLayout);
		
		osText = new StyledStaticTextItem("Operating System");
		memoryText = new StyledStaticTextItem("Defined Memory");
		cpuCoresText = new StyledStaticTextItem("Number of Cores");

		FormIconItem osIcon = new FormIconItem("general/basic_os_icon.png");
		FormIconItem memoryIcon = new FormIconItem("general/basic_memory_icon.png");
		FormIconItem cpuIcon = new FormIconItem("general/basic_cpu_icon.png");
		
		DynamicForm generalDetailsForm = new DynamicForm();
		generalDetailsForm.setNumCols(3);
		generalDetailsForm.setCellPadding(5);
		generalDetailsForm.setItems(osIcon, osText, memoryIcon, memoryText, cpuIcon, cpuCoresText);
		generalDetailsForm.setAutoHeight();
		generalDetailsForm.setWidth100();
		generalDetailsForm.setPadding(20);
		generalDetailsForm.setColWidths("40", "*", "*");
		
		disksLayout.setLayoutLeftMargin(20);
		disksLayout.setLayoutBottomMargin(20);
		disksLayout.setAutoHeight();
		
		HLayout drivesTitleLayout = new HLayout(5);
		drivesTitleLayout.setLayoutLeftMargin(25);
		drivesTitleLayout.setLayoutTopMargin(20);
		drivesTitleLayout.setLayoutBottomMargin(10);
		drivesTitleLayout.setAutoHeight();
		Img driveImg = new Img("general/basic_drive_icon.png", 33, 33);
		UPLabel drivesTitle = new UPLabel("Drives :", "basicInfoDetailsTitle");
		drivesTitle.setLayoutAlign(VerticalAlignment.CENTER);
		drivesTitleLayout.setMembers(driveImg, drivesTitle);
		
		DynamicForm consoleForm = new DynamicForm();
		FormIconItem consoleIcon = new FormIconItem("general/basic_console_icon.png");
		consoleText = new StyledStaticTextItem("Console");
		consoleText.setTextBoxStyle("basicInfoDetailsTitle");

		consoleEditText = new StyledStaticTextItem("");
		consoleEditText.setTextBoxStyle("basicInfoDetailsLink");
		consoleEditText.setShowTitle(false);
		consoleEditText.setValue("(Edit)");
		consoleEditText.setDisabled(true);
		consoleEditText.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ConsoleEditPanel editPanel = new ConsoleEditPanel(currentItem, vmBasicInfoLayout);
				editPanel.draw();
			}
		});
		
		consoleForm.setItems(consoleIcon, consoleText, consoleEditText);
		consoleForm.setNumCols(4);
		consoleForm.setAutoHeight();
		consoleForm.setWidth100();
		consoleForm.setPadding(25);
		consoleForm.setCellPadding(0);
		consoleForm.setColWidths("40", "*", "50", "40");

		consoleMessage = new UPLabel("basicInfoVncMessage");
		consoleMessage.setWrap(true);
		consoleMessage.setWidth100();
		consoleMessage.hide();
		
		addMember(generalInfoLayout);
		addMember(generalDetailsForm);
		addMember(getSeparator());
		addMember(drivesTitleLayout);		
		addMember(disksLayout);
		addMember(getSeparator());
		addMember(consoleForm);
		addMember(consoleMessage);
		
		model.getPropertyChangedEvent().addListener(new DiskListDetailModelInit());
	}

	private HLayout getSeparator() {
		HLayout separator = new HLayout();
		separator.setWidth100();
		separator.setHeight(1);
		separator.setOverflow(Overflow.HIDDEN);
		separator.setStyleName("basicViewInfoLayoutSeparator");
		return separator;
	}
	
	public void updateValues(UserPortalItemModel item, VmTvLayout selectedItemLayout) {
		if (item == null) {
			initInfoLayout();
		}
		else {
			currentItem = item;
			currentItemLayout = selectedItemLayout;
			setOsImage(item.getOsType().name());
			setVmTypeImage(item);
			setVmName(item.getName());
			setVmDescription(item.getDescription());
			
			osText.setValue(item.getOsType().name());
			memoryText.setValue((String)model.getSelectedItemDefinedMemory().getEntity());
			cpuCoresText.setValue((String)model.getSelectedItemNumOfCpuCores().getEntity());

			if (!currentItem.getIsPool()) {
				if (currentItemLayout.getProtocol() == null) {
					consoleMessage.setHtmlContents(currentItemLayout.getProtocolMessage());
					consoleMessage.show();
					consoleEditText.disable();
					consoleText.setValue("");
				}
				else {
					consoleMessage.hide();
					consoleEditText.enable();
					consoleText.setValue(currentItemLayout.getProtocol() == null ? "" : currentItemLayout.getProtocol().displayName);
				}
			}
			else {
				consoleEditText.disable();
				consoleText.setValue("");
			}
		}

	}

	public void setVmName(String name) {
		if (!vmNameLabel.getContents().equals(name))
			vmNameLabel.setContents(name);
	}
	public void setVmDescription(String description) {
		if (!vmDescriptionLabel.getContents().equals(description))
			vmDescriptionLabel.setContents(description);
	}
	public void setOsImage(String imageName) {
		if (!osImage.getSrc().equals(imageName)) {
			osImage.setSrc("os/" + imageName + ".jpg");
		}
		if (!osImage.isVisible())
			osImage.show();
	}
	public void setVmTypeImage(UserPortalItemModel item) { 
		String imageName;
		int width;
		int height;
		if (item.getIsPool()) {
			imageName = "pool_icon";
			width = 24;
			height = 19;
		}
		else if (item.getIsServer()) {
			imageName = "server_vm_icon";
			width = 14;
			height = 21;
		}
		else {
			imageName = "desktop_vm_icon";
			width = 16;
			height = 14;
		}
			
		if (!vmTypeImage.getSrc().equals(imageName)) {
			vmTypeImage.setSrc("vmtypes/" + imageName + ".png");
			vmTypeImage.setLeft(OS_TYPE_IMAGE_SIZE + 5 - width/2);
			vmTypeImage.setTop(OS_TYPE_IMAGE_SIZE - height/2);
			vmTypeImage.setHeight(height);
			vmTypeImage.setWidth(width);
		}
	}
	
	private void initInfoLayout() {
		vmNameLabel.setContents(null);
		vmDescriptionLabel.setContents(null);		
		osImage.hide();
		osText.setValue("");
		memoryText.setValue("");
		cpuCoresText.setValue("");
		vmTypeImage.hide();
		for (Canvas c : disksLayout.getMembers()) {
			c.hide();
		}
	}

	class DiskListDetailModelInit implements IEventListener {
		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {
			PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs)args;

			if (pcArgs.PropertyName.equals("DetailModels")) {
				model.getvmBasicDiskListModel().getItemsChangedEvent().addListener(new IEventListener() {
					@Override
					public void eventRaised(Event ev, Object sender, EventArgs args) {
						updateDisks((List<DiskImage>)model.getvmBasicDiskListModel().getItems());
					}
				});			
			}
		}
	}
	
	class StyledStaticTextItem extends StaticTextItem {
		public StyledStaticTextItem(String title) {
			setTitle(title);
			setWrap(false);
			setWrapTitle(false);
			setTitleStyle("basicInfoDetailsTitle");
			setTextBoxStyle("basicInfoDetailsValue");
		}
	}
	
	class FormIconItem extends CanvasItem {
		public FormIconItem(String imgName) {
			setColSpan(1);
			setShowTitle(false);
			Img image = new Img(imgName, 33, 33);
			setCanvas(image);
		}
	}
	
	class DiskEntry extends HLayout {
		UPLabel sizeLabel;
		UPLabel diskNameLabel;
		long diskSize = -1;
		
		public DiskEntry() {
			setLayoutLeftMargin(70);
			setLayoutRightMargin(25);
			setAutoHeight();
			setWidth100();
			
			sizeLabel = new UPLabel("basicInfoDetailsValue");
			sizeLabel.setWidth100();

			diskNameLabel = new UPLabel("basicInfoDetailsValueBold");
			
			setMembers(diskNameLabel, sizeLabel);
		}
		
		public void setDiskSize(long size) {
			if (diskSize != size) {
				diskSize = size;
				sizeLabel.setHtmlContents("<b>" + diskSize + "GB</b>");
			}
		}
		
		public void setDiskName(String name) {
			if (!diskNameLabel.getContents().equals(name))
				diskNameLabel.setContents(name);
		}
	}

	private ArrayList<DiskEntry> diskEntries = new ArrayList<DiskEntry>();
	private void updateDisks(List<DiskImage> disks) {
		if (disks.size() > diskEntries.size()) {
			for (int i=diskEntries.size(); i<disks.size(); i++) {
				DiskEntry diskEntry = new DiskEntry();
				disksLayout.addMember(diskEntry);
				diskEntries.add(diskEntry);
			}
		}			
		else {
			for (int i=disks.size(); i<diskEntries.size(); i++) {
				diskEntries.get(i).hide();
			}
		}
		
		for (int i=0; i<disks.size(); i++) {
			DiskEntry currEntry = diskEntries.get(i); 
			if (!currEntry.isVisible())
				currEntry.show();
			
			DiskImage currDisk = disks.get(i);
			
			//currEntry.setSizeLabel((diskSize == 0 ? "Less than <b>1" : "<b>" + diskSize) + "GB</b> out of <b>" + currDisk.getSizeInGigabytes() + "GB</b>");
			//currEntry.setDiskName("Disk " + currDisk.getinternal_drive_mapping() + ':');
			currEntry.setDiskSize(currDisk.getSizeInGigabytes());
			currEntry.setDiskName("Disk " + currDisk.getinternal_drive_mapping() + ':');
		}
	}

	@Override
	public void setProtocol(Protocol protocol) {
		currentItemLayout.setProtocol(protocol);
		consoleText.setValue(currentItemLayout.getProtocol() == null ? "None" : currentItemLayout.getProtocol().displayName);
	}

	@Override
	public Protocol getProtocol() {
		return currentItemLayout.getProtocol();
	}

	@Override
	public void setProtocolMessage(String message) {
		currentItemLayout.setProtocolMessage(message);
	}

	@Override
	public String getProtocolMessage() {
		return currentItemLayout.getProtocolMessage();
	}
}
