package org.ovirt.engine.ui.userportal.client.modalpanels;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommon.models.users.AdElementListModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.UPTextItem;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameIdResolver;
import org.ovirt.engine.ui.userportal.client.timers.SubTabRefreshTimer;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class AddPermissionsModalPanel extends NonDraggableModalPanel {

	final AddPermissionsModalPanel addPermissionsModalPanel = this;
	UICommand cancelCommand;
	
	public AddPermissionsModalPanel(final PermissionListModel permissionListModel) {
		super(550, 450, "Add Permission to User");

		final AdElementListModel adElementListModel = (AdElementListModel)permissionListModel.getWindow();
		VLayout inPanel = new VLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		
		SelectBoxListModelBinded domainBox = new SelectBoxListModelBinded("Search", adElementListModel.getDomain(), new ObjectNameIdResolver() {
			@Override
			public String getItemName(Object o) {
				return (String)o;
			}
			@Override
			public String getItemId(Object o) {
				return (String)o;
			}
		});
	
		final UPTextItem searchStringBox = new UPTextItem();
		searchStringBox.setShowTitle(false);
		searchStringBox.setWidth(295);
		searchStringBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if ((event.getCharacterValue() != null)	&& (event.getCharacterValue() == KeyCodes.KEY_ENTER)) {
					adElementListModel.setSearchString((String)searchStringBox.getValue());
					adElementListModel.getSearchCommand().Execute();
				}
			}
		});
		
		final Button goButton = new Button("GO");
		goButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				adElementListModel.setSearchString((String)searchStringBox.getValue());
				adElementListModel.getSearchCommand().Execute();
			}
		});
		
		final ListGrid usersGrid = new ListGrid();
		usersGrid.setLeaveScrollbarGap(false);
		usersGrid.setShowHeaderContextMenu(false);
		usersGrid.setShowAllRecords(true);
		usersGrid.setEmptyMessage("");

		ListGridField isSelectedField = new ListGridField("isSelected", " ");
		isSelectedField.setType(ListGridFieldType.BOOLEAN);
		isSelectedField.setWidth(25);
//		Not needed anymore since toggling of the selection is made through row click
//		isSelectedField.setCanToggle(true);
//		isSelectedField.setCanEdit(true);
		
		ListGridField typeField = new ListGridField("type", " ");
		typeField.setType(ListGridFieldType.IMAGE);
		typeField.setImageURLPrefix("types/");
		typeField.setImageURLSuffix(".png");
		typeField.setAttribute("imageWidth", "typeImageWidth");
		typeField.setAttribute("imageHeight", "typeImageHeight");
		typeField.setWidth(30);
		typeField.setAlign(Alignment.CENTER);
		
		ListGridField firstNameField = new ListGridField("firstName", "First Name");
		firstNameField.setWidth("25%");
		ListGridField lastNameField = new ListGridField("lastName", "Last Name");
		lastNameField.setWidth("25%");
		ListGridField userNameField = new ListGridField("userName", "User Name");
		userNameField.setWidth("50%");
		usersGrid.setFields(isSelectedField, typeField, firstNameField, lastNameField, userNameField);
		
		usersGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				if (event.getRecord() != null && event.getState()) {	
					event.getRecord().setAttribute("isSelected", !event.getRecord().getAttributeAsBoolean("isSelected"));
					usersGrid.markForRedraw();
				}
				usersGrid.deselectAllRecords();
			}
		});
		
		adElementListModel.getItemsChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				usersGrid.setData(calcRecordsFromItems());
			}

			public RecordList calcRecordsFromItems() {
				RecordList records = new RecordList();
				if (adElementListModel.getItems() != null) {
					for (Object o : adElementListModel.getItems()) {
						DbUser user = (DbUser)((EntityModel)o).getEntity();
						ListGridRecord r = new ListGridRecord();
						r.setAttribute("firstName", user.getname());
						r.setAttribute("lastName", user.getsurname());
						r.setAttribute("userName", user.getusername());
						r.setAttribute("type", user.getIsGroup() ? "GROUP" : "USER");
						r.setAttribute("typeImageWidth", user.getIsGroup() ? 16 : 9);
						r.setAttribute("typeImageHeight", user.getIsGroup() ? 15: 13);
						r.setAttribute("entity", o);
						r.setAttribute("isSelected", ((EntityModel)o).getIsSelected());
						records.add(r);
					}
				}
				return records;
			}
		});
		
		SelectBoxListModelBinded roleBox = new SelectBoxListModelBinded("Role to Assign", adElementListModel.getRole(), new ObjectNameIdResolver() {
			@Override
			public String getItemName(Object o) {
				return ((roles)o).getname();
			}
			
			@Override
			public String getItemId(Object o) {
				return ((roles)o).getId().toString();
			}
		});
		
		
		HLayout searchLayout = new HLayout();
		
		DynamicForm domainBoxForm = new DynamicForm();
		DynamicForm searchStringBoxForm = new DynamicForm();
		DynamicForm roleBoxForm = new DynamicForm();
		domainBoxForm.setItems(domainBox);
		searchStringBoxForm.setItems(searchStringBox);
		searchStringBoxForm.setAutoFocus(true);
		
		roleBoxForm.setItems(roleBox);
		roleBoxForm.setStyleName("permissionsRoleBox");
		
		searchLayout.setMembers(domainBoxForm, searchStringBoxForm, goButton);
		searchLayout.setAutoHeight();
		
		inPanel.addMember(searchLayout);
		inPanel.addMember(usersGrid);
		inPanel.addMember(roleBoxForm);
		inPanel.setMembersMargin(3);
		addItem(inPanel);		
	
		permissionListModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				if (propertyName.equals("Window")) {
					permissionListModel.getPropertyChangedEvent().removeListener(this);
					destroy();
				}
			}
		});
		
		final UICommand saveCommand = new UICommand("OnAdd", permissionListModel);
		cancelCommand = new UICommand("Cancel", permissionListModel);
		
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<EntityModel> selectedUsers = new ArrayList<EntityModel>();
				
				for (ListGridRecord r : usersGrid.getRecords()) {
					if (r.getAttributeAsBoolean("isSelected")) {
						EntityModel em = (EntityModel)r.getAttributeAsObject("entity");
						em.setIsSelected(true);
						selectedUsers.add(em);
					}
				}

				adElementListModel.setSelectedItems(selectedUsers);
				
				SubTabRefreshTimer.initSubTabRefreshTimer(permissionListModel);
				saveCommand.Execute();
			}
		});
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
			}
		});
		
		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
		
        subscribeProgressChangedEvent(adElementListModel, searchStringBox, inPanel);
	}

	public void onClose() {
		cancelCommand.Execute(); 
	}
}
