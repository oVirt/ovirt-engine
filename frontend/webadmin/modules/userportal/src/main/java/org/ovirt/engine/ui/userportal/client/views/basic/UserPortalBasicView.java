package org.ovirt.engine.ui.userportal.client.views.basic;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.client.Masthead;
import org.ovirt.engine.ui.userportal.client.UserPortal;
import org.ovirt.engine.ui.userportal.client.common.UserPortalMode;
import org.ovirt.engine.ui.userportal.client.components.RefreshPanel;
import org.ovirt.engine.ui.userportal.client.events.SelectedItemChangedEventArgs;
import org.ovirt.engine.ui.userportal.client.views.basic.components.VmTvLayout;
import org.ovirt.engine.ui.userportal.client.components.GridRefreshManager;

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

public class UserPortalBasicView extends VLayout {
	
	VmTileGrid vmTileGrid;
	VmBasicInfoLayout vmBasicInfoLayout;
	UserPortalBasicListModel upblm;
	
	public UserPortalBasicView() {
		setWidth100();
		setHeight100();
		setShowEdges(true);
		setEdgeImage("edges/lightblueframe.png");
		setEdgeSize(4);         
			
		upblm = new UserPortalBasicListModel();
		vmTileGrid = new VmTileGrid();
		vmTileGrid.setModel(upblm);

		vmBasicInfoLayout = new VmBasicInfoLayout(upblm);

		vmTileGrid.getSelectionChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				SelectedItemChangedEventArgs upArgs = (SelectedItemChangedEventArgs)args;
				vmBasicInfoLayout.updateValues((UserPortalItemModel)upArgs.selectedItem, (VmTvLayout)vmTileGrid.getSelectedItemLayout());
			}
		});

		vmTileGrid.getItemsChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				UserPortalItemModel selectedItem = (UserPortalItemModel)upblm.getSelectedItem();
				if (selectedItem == null)
					return;
				vmBasicInfoLayout.updateValues(selectedItem, (VmTvLayout)vmTileGrid.getSelectedItemLayout());
			}
		});
		
		vmTileGrid.getItemsChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
			    if (Masthead.getUserPortalMode() == UserPortalMode.BASIC && 
				UserPortal.getSessionConnectAutomatically() && upblm.getCanConnectAutomatically())
		    	    {
				UserPortalItemModel userPortalItemModel = upblm.GetStatusUpVms(upblm.getItems()).get(0);
				
				if (userPortalItemModel != null)
				{
				    userPortalItemModel.getDefaultConsole().getConnectCommand().Execute();
				    UserPortal.setSessionConnectAutomatically(false);
				}			
		    	    }
			    vmTileGrid.getItemsChangedEvent().removeListener(this);
			}
		});		
		
		vmTileGrid.getLayout().setWidth("60%");
		vmBasicInfoLayout.setWidth("40%");
		
		HLayout viewWrapper = new HLayout();
		viewWrapper.setStyleName("basicViewLayout");
		viewWrapper.addMember(vmTileGrid.getLayout());
		viewWrapper.addMember(vmBasicInfoLayout);
		
		addMember(getToolbar());
		addMember(viewWrapper);
	
	}
	
	public ToolStrip getToolbar() {
        ToolStrip toolBar = new ToolStrip();
        toolBar.setBackgroundColor("#FFFFFF");
        toolBar.setWidth100();
        toolBar.setHeight(28);

        toolBar.setMembers(new LayoutSpacer(), new RefreshPanel(vmTileGrid));
        
        return toolBar;
    }
	
	@Override
    public void show() {
        super.show();
        GridRefreshManager.getInstance().subscribe(vmTileGrid);
    }
    
   @Override
    public void hide() {
        super.hide();
        GridRefreshManager.getInstance().unsubscribe(vmTileGrid);
    }
}
