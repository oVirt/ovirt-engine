package org.ovirt.engine.ui.userportal.client.views.extended;

import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.MainGrid;
import org.ovirt.engine.ui.userportal.client.views.extended.resources.ResourcesView;
import org.ovirt.engine.ui.userportal.client.views.extended.templates.TemplateGridLayout;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

public class UserPortalExtendedView extends HLayout {
	
	VLayout tabSection = new VLayout();
	Layout tabPaneContainer = new Layout();
	TabButton selectedButton;
	
	public UserPortalExtendedView() {
		tabSection.setWidth(150);
		tabSection.setAutoHeight();		
		tabSection.setMembersMargin(5);
		tabSection.setStyleName("mainTabSection");
		
		tabPaneContainer.setWidth100();
		tabPaneContainer.setHeight100();
		tabPaneContainer.setShowEdges(true);
		tabPaneContainer.setEdgeImage("edges/lightblueframe.png");
		tabPaneContainer.setEdgeSize(4);

        TabButton vmsButton = new TabButton("Virtual Machines", new MainGrid());
		TabButton templatesButton = new TabButton("Templates", new TemplateGridLayout());
		TabButton resourcesButton = new TabButton("Resources", new ResourcesView());
		
		selectedButton = vmsButton;
		vmsButton.select();
		
		setMembers(tabSection, tabPaneContainer);	
	}

	class TabButton extends Button {
		private Canvas pane;

		public TabButton(String title, Canvas pane) {
			super(title);
			setBaseStyle("mainTabButton");
			setWidth100();
			setHeight(25);			
			this.pane = pane;
			pane.setVisible(false);
			tabSection.addMember(this);
			tabPaneContainer.addMember(pane);

			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (selectedButton != TabButton.this) {
						selectedButton.deselect();
						selectedButton = TabButton.this;
						selectedButton.select();
					}
				}
			});
		}
		
		@Override
	    public void select() {
			super.select();
			pane.show();			
		}
		@Override
    	public void deselect() {
			super.deselect();
			pane.hide();			
		}
	}
	
	@Override
    public void show() {
        super.show();
        selectedButton.select();
    }
    
   @Override
    public void hide() {
        super.hide();
        selectedButton.deselect();
    }
}
