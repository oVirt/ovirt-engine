package org.ovirt.engine.ui.userportal.client.modalpanels;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommon.models.vms.UnitVmModel;
import org.ovirt.engine.ui.userportal.client.components.CheckboxItemModelBinded;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.components.TextItemEntityModelBinded;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.MainGrid;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.layout.HLayout;

public class NewTemplateModalPanel extends NonDraggableModalPanel {
	final NewTemplateModalPanel newTemplateModalPanel = this;
	final MainGrid mainGrid;
	final UnitVmModel vmModel;
	final UserPortalListModel userPortalListModel;
	UICommand cancelCommand;
	
	Button okButton;	
	Button cancelButton;
	Button closeButton;
	
	List<TextItemEntityModelBindedWrapper> textItems = new ArrayList<TextItemEntityModelBindedWrapper>();
	
	public NewTemplateModalPanel(String title, final MainGrid mainGrid) {
		super(300, 260, title);
		
		this.mainGrid = mainGrid;
		userPortalListModel = mainGrid.uplm;
		vmModel = userPortalListModel.getVmModel();
		
		
		// Creating and set a inner panel
		HLayout inPanel = new HLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		inPanel.setAlign(Alignment.CENTER);
		
		// Create name and description text boxes
		TextItemEntityModelBindedWrapper nameTextItem = new TextItemEntityModelBindedWrapper("Name", vmModel.getName());
		TextItemEntityModelBindedWrapper descriptionTextItem = new TextItemEntityModelBindedWrapper("Description", vmModel.getDescription());

		// Create cluster and storage select boxes
		SelectBoxListModelBinded clusterBox = new SelectBoxListModelBinded("Host Cluster", vmModel.getCluster(), VDSGroup.class);
		SelectBoxListModelBinded storageBox = new SelectBoxListModelBinded("Storage Domain", vmModel.getStorageDomain(), storage_domains.class);
		
		// Create 'Make Public' check box
		CheckboxItemModelBinded isPublicCheckBox = new CheckboxItemModelBinded("Make Public", vmModel.getIsTemplatePublic());
		
		// Creating error message label 		
		final HeaderItem messageLabel = new HeaderItem();
		messageLabel.setDefaultValue(vmModel.getMessage());
		messageLabel.setTextBoxStyle("warningLabel");
		
		// Creating and set an indent spacer
		SpacerItem indentSpacer = new SpacerItem();
		indentSpacer.setWidth(3);
		indentSpacer.setHeight(5);
		
		/** Add components to panel **/

		// Set items inside a form
		DynamicForm f = new DynamicForm();
		f.setItems(nameTextItem, descriptionTextItem, clusterBox, storageBox, isPublicCheckBox, indentSpacer, messageLabel);

		// Adding components to inner panel
		inPanel.addMember(f);

		// Adding inner panel to this view
		addItem(inPanel);

		// Add footer buttons
		createFooterButtons();
		updateFooterButtons(vmModel.getIsValid());
		
		vmModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
			    String changedPropertyName = ((PropertyChangedEventArgs)args).PropertyName;
			    if (changedPropertyName.equals("Message"))
			    {
			    	messageLabel.setValue(vmModel.getMessage());
			    	updateFooterButtons(vmModel.getIsValid());
			    }
			}
		});
	}
	
	private void createFooterButtons() {		

		userPortalListModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				String propertyName = ((PropertyChangedEventArgs)args).PropertyName;
				if (propertyName.equals("VmModel")) {
					userPortalListModel.getPropertyChangedEvent().removeListener(this);
					destroy();
				}
			}
		});
		
    	final UICommand saveCommand = new UICommand("OnNewTemplate", userPortalListModel);
       	cancelCommand = new UICommand("Cancel", userPortalListModel);
        		
		closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
		    	onClose();
		    }
		});
		cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
		    	onClose();
		    }
		});
		okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
		    	saveCommand.Execute();
		    }
		});
	}
	
	private void updateFooterButtons(boolean isValid) {	    
	    if (isValid)
	    	setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
	    else
	    	setFooterButtons(Alignment.RIGHT, closeButton);
	}
	
	// Wraps TextItemEntityModelBinded in order to add all local components to
	// be added to a validation list
	class TextItemEntityModelBindedWrapper extends TextItemEntityModelBinded {
	    public TextItemEntityModelBindedWrapper(String title, EntityModel model) {
	    	super(title, model);
			textItems.add(this);
	    }
	}
	
	@Override
	public void onClose() {
    	cancelCommand.Execute();
	}
}