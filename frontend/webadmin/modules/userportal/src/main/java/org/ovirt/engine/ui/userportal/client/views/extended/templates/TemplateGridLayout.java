package org.ovirt.engine.ui.userportal.client.views.extended.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommon.models.vms.UnitVmModel;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.GridRefreshManager;
import org.ovirt.engine.ui.userportal.client.components.RefreshPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewVmModalPanel;
import org.ovirt.engine.ui.userportal.client.views.extended.templates.components.TemplateItemSubTabs;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.toolbar.ToolStripSeparator;

public class TemplateGridLayout extends VLayout {
	
	private UserPortalTemplateListModel tlm = new UserPortalTemplateListModel();
	
	private ToolStripButton editButton;
	private ToolStripButton removeButton;

	private TemplateGrid templatesGrid; 
	private TemplateItemSubTabs templateItemSubTabs;
	
	public TemplateGridLayout() {
		setOverflow(Overflow.HIDDEN);
		setWidth100();
		setHeight100();
		
		templatesGrid = new TemplateGrid();
		templatesGrid.setModel(tlm);
		templatesGrid.search();
				
		templateItemSubTabs = new TemplateItemSubTabs(tlm, templatesGrid);
		
		addMember(getToolbar());
		addMember(templatesGrid.getLayout());
		addMember(templateItemSubTabs);
		
		// At the initial item selection in TemplateListModel, the detail models are initialized 
		tlm.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs pcea = (PropertyChangedEventArgs)args;
				if (pcea.PropertyName.equals("DetailModels")) {
					initItemSelected();
				}
			}
		});		
		
		templatesGrid.getSelectionStatusChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				if (args == GridController.ITEM_SELECTED_EVENT_ARGS) {
					templateItemSubTabs.show();
				}
				else {
					templateItemSubTabs.hide();
				}
			}
		});
	}

	private ToolStrip getToolbar() {
		ToolStrip toolBar = new ToolStrip();
		toolBar.setWidth100();
		toolBar.setBackgroundColor("#FFFFFF");
		toolBar.setStyleName("mainGrid-Toolbar");
		
		editButton = new ToolStripButton("Edit");
		editButton.setAutoFit(true);
		editButton.addClickHandler(editButtonClickHandler);
		editButton.setHeight(19);
		editButton.setDisabled(true);
		
		removeButton = new ToolStripButton("Remove");
		removeButton.setAutoFit(true);
		removeButton.addClickHandler(removeButtonClickHandler);
		removeButton.setHeight(19);
		removeButton.setDisabled(true);
		
		setVisibilityHandlers();
		
		toolBar.setMembers(editButton, new ToolStripSeparator(), removeButton, new LayoutSpacer(), new RefreshPanel(templatesGrid));
		
		return toolBar;
	}
	
	
	ClickHandler removeButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			tlm.getRemoveCommand().Execute();
			Window removeTemplateModalPanel = new ItemRemoveModalPanel("Remove Template(s)", "Template(s)", tlm, new ObjectNameResolver() {
				@Override
				public String getItemName(Object o) {
					return ((VmTemplate)o).getname();
				}
			}, templatesGrid);
			removeTemplateModalPanel.draw();
		}
	};
	
	ClickHandler editButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			tlm.getEditCommand().Execute();
			Window editTemplateModalPanel = new NewVmModalPanel(templatesGrid, (UnitVmModel)tlm.getWindow(), tlm);
			editTemplateModalPanel.draw();
		}
	};
	
	@Override
	public void show() {
		super.show();
		GridRefreshManager.getInstance().subscribe(templatesGrid);	
	}
	
    @Override
    public void hide() {
        super.hide();
        GridRefreshManager.getInstance().unsubscribe(templatesGrid);
    }

	private void setVisibilityHandlers() {
		tlm.getEditCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				editButton.setDisabled(!tlm.getEditCommand().getIsExecutionAllowed());
			}
		});

		tlm.getRemoveCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				removeButton.setDisabled(!tlm.getRemoveCommand().getIsExecutionAllowed());
			}
		});

	}
	
	public void initItemSelected() {
		removeButton.setDisabled(!tlm.getRemoveCommand().getIsExecutionAllowed());
		editButton.setDisabled(!tlm.getEditCommand().getIsExecutionAllowed());
	}
}
