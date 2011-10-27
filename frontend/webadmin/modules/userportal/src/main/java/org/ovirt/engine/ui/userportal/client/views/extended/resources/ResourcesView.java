package org.ovirt.engine.ui.userportal.client.views.extended.resources;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.resources.ResourcesModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.components.MonitorBar;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class ResourcesView extends VLayout{
	private ResourcesModel resourcesModel = new ResourcesModel();
	private StorageGrid storageGrid;
	
	public ResourcesView() {

		setPadding(10);
		setHeight100();
		setWidth100();
		setMembersMargin(20);
		setMembers(getBarsSection(), getStorageSection());

		resourcesModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				if (((PropertyChangedEventArgs)args).PropertyName.equals("Items")) {
					setStorageGridData();
				}
			}
		});
	}
	
	private HStack getBarsSection() {
		HStack barsSection = new HStack();
		barsSection.setWidth100();
		barsSection.setAutoHeight();
		
		ResourceInfoLayout vmsLayout = new ResourceInfoLayout("Virtual Machines", "general/vm_icon.png", resourcesModel.getRunningVMsPercentage(), "#97B7D7", "Defined VMs:", resourcesModel.getDefinedVMs(), "Running VMs:", resourcesModel.getRunningVMs(), Alignment.LEFT); 
		ResourceInfoLayout cpuLayout = new ResourceInfoLayout("Virtual CPUs", "general/cpu_icon.png", resourcesModel.getUsedCPUsPercentage(), "#97B7D7", "Defined vCPUs:", resourcesModel.getDefinedCPUs(), "Used vCPUs:", resourcesModel.getUsedCPUs(), Alignment.CENTER); 
		ResourceInfoLayout memoryLayout = new ResourceInfoLayout("Memory", "general/memory_icon.png", resourcesModel.getUsedMemoryPercentage(), "#97B7D7", "Defined Memory:", resourcesModel.getDefinedMemory(),  "Memory Usage:", resourcesModel.getUsedMemory(),  Alignment.RIGHT);
		
		barsSection.setMembers(vmsLayout, cpuLayout, memoryLayout);
		return barsSection;
	}
	
	private VLayout getStorageSection() {
		VLayout storageSection = new VLayout(10);
		storageSection.setWidth("99%");
		storageSection.setHeight100();
		storageSection.setShowEdges(true);
		storageSection.setEdgeImage("edges/light.png");
		storageSection.setEdgeSize(7);
		storageSection.setPadding(5);
		
		ResourcesLabel storageLabel = new ResourcesLabel("Storage:", "resourcesGeneralLabel", true);
		
		ResourcesLabel totalSizeLabel = new ResourcesLabel("Total Size:", "resourcesLabel"); 
		ResourcesLabel totalSize = new ResourcesLabel(resourcesModel.getTotalDisksSize(), "resourcesValue"); 
		ResourcesLabel numSnapshotsLabel = new ResourcesLabel("Number of Snapshots:", "resourcesLabel"); 
		ResourcesLabel numSnapshots = new ResourcesLabel(resourcesModel.getNumOfSnapshots(), "resourcesValue"); 
		ResourcesLabel totalSizeSnapshotsLabel = new ResourcesLabel("Total Size of Snapshots:", "resourcesLabel"); 
		ResourcesLabel totalSizeSnapshots = new ResourcesLabel(resourcesModel.getTotalSnapshotsSize(), "resourcesValue"); 
		
		
		DynamicForm infoLayout = new DynamicForm();
		infoLayout.setAutoWidth();
		infoLayout.setItems(storageLabel, totalSizeLabel, totalSize, numSnapshotsLabel, numSnapshots, totalSizeSnapshotsLabel, totalSizeSnapshots);

		Img storageIcon = new Img("general/storage_icon.png", 32, 32);
		
		HLayout infoWithIcon = new HLayout(5);
		infoWithIcon.setMembers(storageIcon, infoLayout);
		
		storageGrid = new StorageGrid();
		
		storageSection.setMembers(infoWithIcon, storageGrid);
		return storageSection;
	}

	@Override
	public void show() {
		super.show();
		resourcesModel.Search();		
	}
	
	
	class ResourceInfoLayout extends VLayout {
		public ResourceInfoLayout(String title, String iconName, final EntityModel percentage, String color, String title1, final EntityModel value1, String title2, EntityModel value2, Alignment alignment) {
			setAutoHeight();
			setWidth("33%");
		
			final MonitorBar bar = new MonitorBar(title, color, 200, 15, "resourcesGeneralLabel");

			percentage.getEntityChangedEvent().addListener(new IEventListener() {
				@Override
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					bar.setBarPercentage((Integer)percentage.getEntity());
				}
			});
			
			ResourcesLabel label1 = new ResourcesLabel(title1, "resourcesLabel");
			final ResourcesLabel label1Value = new ResourcesLabel(value1, "resourcesValue");
			ResourcesLabel label2 = new ResourcesLabel(title2, "resourcesLabel");
			final ResourcesLabel label2Value = new ResourcesLabel(value2, "resourcesValue");
			
			
			DynamicForm infoLayout = new DynamicForm();
			infoLayout.setNumCols(2);
			infoLayout.setColWidths("*", "*");
			infoLayout.setItems(label1, label1Value, label2, label2Value);
			infoLayout.setCellPadding(0);
			
			VLayout barInfo = new VLayout(5);
			barInfo.setMembers(bar, infoLayout);
			
			Img icon = new Img(iconName, 32,32);
			
			HLayout wrapper = new HLayout(7);
			wrapper.setAutoWidth();
			wrapper.setAutoHeight();
			wrapper.setShowEdges(true);
			wrapper.setEdgeImage("edges/light.png");
			wrapper.setEdgeSize(7);
			wrapper.setPadding(5);
			wrapper.setMembers(icon, barInfo);
			wrapper.setLayoutAlign(alignment);

			setMembers(wrapper);
		}
	}
	
	class ResourcesLabel extends HeaderItem {
		public ResourcesLabel(String value, String style, boolean endLine) {
			if (endLine != true) {
				setColSpan(1);
				setEndRow(false);
				setStartRow(false);
			}
			setTextBoxStyle(style);
			setDefaultValue(value);
		}
		
		public ResourcesLabel(String value, String style) {
			this(value, style, false);
		}
		
		public ResourcesLabel(final EntityModel value, String style) {
			this("", style, false);
			value.getEntityChangedEvent().addListener(new IEventListener() {
				@Override
				public void eventRaised(Event ev, Object sender, EventArgs args) {
					setDefaultValue(value.getEntity().toString());
					redraw();
				}
			});
		}
		
		
	}

	class StorageGrid extends TreeGrid {
		public StorageGrid() {
			setBaseStyle("vmGridRowStyle");
			setStyleName("vmGridRowStyle");
			setHeight100();
			setWidth100();
			setCanSort(false);
			setShowHeaderContextMenu(false);
			setLeaveScrollbarGap(false);
			setFolderIcon("general/vm_icon.gif");
			setClosedIconSuffix("");
			setShowOpenIcons(false);
			setNodeIcon("general/disk_icon.gif");
			setIconSize(23);
			setAnimateFolders(false);
			setShowEmptyMessage(false);
			
			TreeGridField name = new TreeGridField("name", "Virtual Machine");
			TreeGridField disks = new TreeGridField("disks", "Disks");
			disks.setWidth(60);
			TreeGridField virtualSize = new TreeGridField("virtualSize", "Virtual Size");
			virtualSize.setWidth(90);
			TreeGridField actualSize = new TreeGridField("actualSize", "Actual Size");
			actualSize.setWidth(90);
			TreeGridField snapshots = new TreeGridField("snapshots", "Snapshots");
			
			setFields(name, disks, virtualSize, actualSize, snapshots);
		}
	}

	public void setStorageGridData() {

		Tree tree = new Tree();
		TreeNode rootNode = new TreeNode();
		tree.setRoot(rootNode);

		if (resourcesModel.getItems() != null) {
			Iterator<VM> iterator = resourcesModel.getItems().iterator();
			while (iterator.hasNext()) {
				VM currVM = iterator.next();
				TreeNode vmNode = new TreeNode();
				vmNode.setAttribute("name", currVM.getvm_name());
				vmNode.setAttribute("disks", currVM.getDiskList().size());
				vmNode.setAttribute("virtualSize", ((Double)currVM.getDiskSize()).intValue() + "GB");
				vmNode.setAttribute("actualSize", ((Double)currVM.getActualDiskWithSnapshotsSize()).intValue() + "GB");
				vmNode.setAttribute("snapshots", currVM.getDiskList().size()>0 ? currVM.getDiskList().get(0).getSnapshots().size() : 0);
				tree.add(vmNode, rootNode);
				
				if (currVM.getDiskList() != null && currVM.getDiskList().size()>0) {
					for (DiskImage disk : currVM.getDiskList()) {
						TreeNode diskNode = new TreeNode();
						diskNode.set_baseStyle("[NONE]");
						diskNode.setAttribute("name", "Disk" + disk.getinternal_drive_mapping());
						diskNode.setAttribute("virtualSize", disk.getSizeInGigabytes() + "GB");
						diskNode.setAttribute("actualSize", ((Double)disk.getActualDiskWithSnapshotsSize()).intValue() + "GB");
						diskNode.setAttribute("snapshots", disk.getSnapshots().size());
						tree.add(diskNode, vmNode);
					}
				}
				else {
					vmNode.setIcon("general/vm_icon.gif");
				}
			}
		}
		storageGrid.setData(tree);
	}
}
