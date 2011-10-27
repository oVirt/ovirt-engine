package org.ovirt.engine.ui.userportal.client.binders.specific;

import java.util.ArrayList;

import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelDualGridBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewSnapshotModalPanel;
import org.ovirt.engine.ui.userportal.client.util.UserPortalTools;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class VmSnapshotListModelBinder implements ListModelDualGridBinder {

	private VmSnapshotListModel model;

	private RendererType rendererType = RendererType.DualGrid;
	
	private String propertyName = "Apps";
	
	@Override
	public RecordList calcRecords() {
		RecordList records = new RecordList();
		ArrayList<SnapshotModel> snapshots = (ArrayList<SnapshotModel>)model.getItems();
		
		if (snapshots != null) {			
			for (SnapshotModel snapshot : snapshots) {
				ListGridRecord r = new ListGridRecord();				
				r.setAttribute("date", snapshot.getDate() != null ? UserPortalTools.formatDate(snapshot.getDate()) : "Current");
				r.setAttribute("disks", snapshot.getParticipantDisks());
				r.setAttribute("entity", snapshot);
				r.setAttribute("entityGuid", snapshot.getSnapshotId());
				r.setEnabled(!snapshot.getIsCurrent());
				
				// Get raw description string (ignore < and > characters.
				// Customize description style if needed.
				String descriptionStr = SafeHtmlUtils.fromString(snapshot.getDescriptionValue()).asString();
				if (snapshot.getIsCurrent()) descriptionStr = "<b><font color=gray>" + descriptionStr + "</font></b>";				
				if (snapshot.getIsPreviewed()) descriptionStr = "<b><font color=orange>" + descriptionStr + "</font></b>";
				r.setAttribute("description", descriptionStr);
				
				records.add(r);
			}
		}
		
		return records;
	}

	@Override
	public void setModel(Model model) {
		this.model = (VmSnapshotListModel)model;
	}

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}

	@Override
	public ListGridField[] getFields() {
		return fields;
	}
	
	private static ListGridField[] fields = {
		new ListGridField("date", "Date", 120),
		new ListGridField("description", "Description"),
		new ListGridField("disks", "Disks", 120)
	};

	@Override
	public ToolbarAction[] getCommands(final GridController gridController) {
		ToolbarAction[] actions = new ToolbarAction[] {
				new ToolbarAction(model.getNewCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getNewCommand().Execute();
						NewSnapshotModalPanel newSnapshotModalPanel = new NewSnapshotModalPanel(gridController, model);
						newSnapshotModalPanel.draw();
					}
				}),

				new ToolbarAction(model.getPreviewCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getPreviewCommand().Execute();
						gridController.gridChangePerformed();
					}
				}),

				new ToolbarAction(model.getCommitCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getCommitCommand().Execute();
					}
				}),

				new ToolbarAction(model.getUndoCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getUndoCommand().Execute();
						gridController.gridChangePerformed();
					}
				}),

				new ToolbarAction(model.getRemoveCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getRemoveCommand().Execute();
						ItemRemoveModalPanel p = new ItemRemoveModalPanel(model.getWindow().getTitle(), "Snapshot", model, new ObjectNameResolver() {
							@Override
							public String getItemName(Object o) {
								String date = UserPortalTools.formatDate(((SnapshotModel)o).getDate());
								String description = ((SnapshotModel)o).getDescriptionValue();
								return "Snapshot from " + date + " with description '" + description + "'";
							}
						}, gridController);
						p.draw();
					}
				})};
		return actions;
	}

	@Override
	public ListGridField[] getAdditionalFields() {
		return additionalFields;
	}
	
	private static ListGridField[] additionalFields = {
		new ListGridField("installedApplications", "Installed Applications")
	};
	
	@Override
	public RecordList calcAdditionalRecords() {
		RecordList records = new RecordList();
		
		ArrayList<String> applications = (ArrayList<String>)model.getApps();				
		if (applications != null) {							
			for (String application : applications) {
				if (!application.isEmpty())
				{
					ListGridRecord r = new ListGridRecord();
					r.setAttribute("installedApplications", application);				
					records.add(r);
				}
			}			
		}
		return records;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

}
