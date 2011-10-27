package org.ovirt.engine.ui.userportal.client.components;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class TabPanel extends VLayout {
	
	private static final int TAB_BAR_THICKNESS = 27;
	private static final int TABS_MARGIN = 5;
	
	private HLayout tabsSection; 
	private HLayout viewSection; 
	private Tab selectedTab;
	
	public TabPanel() {
		
		tabsSection = new HLayout(TABS_MARGIN);
		tabsSection.setWidth100();
		tabsSection.setHeight(TAB_BAR_THICKNESS);
		tabsSection.setLayoutLeftMargin(5);
		tabsSection.setStyleName("tabPanelTabsSection");

		viewSection = new HLayout();
		viewSection.setWidth100();
		viewSection.setHeight100();
		viewSection.setBorder("1px solid #C0C3C7");
		
		viewSection.setZIndex(-1);
		setMembersMargin(-1);
		addMember(tabsSection);
		addMember(viewSection);
	}

	public void addTab(Tab tab) {
		addTab(tab, false);
	}
	
	public void addTab(final Tab tab, boolean isHidden) {
		tab.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GWT.log("Active detail tab changed to " + tab.getTitle() + " by tab click");
				select(tab);
			}
		});
		
		tabsSection.addMember(tab);
		
		tab.getPane().hide();

		viewSection.addChild(tab.getPane());

		if (isHidden) 
			hideTab(tab);
		else 
			if (selectedTab == null)
				select(tab);
	}
	
    public Tab getSelectedTab() {
    	return selectedTab;
    }

    public void select(Tab tab) {
		if (selectedTab != null) {
			if (selectedTab.equals(tab))
				return;
			
			selectedTab.getPane().hide();
			selectedTab.deselect();
		}
		
    	selectedTab = tab;
    	selectedTab.select();
		tab.getPane().show();
    }

    public void hideTab(Tab tab) {
    	tab.hide();
    }

    public void showTab(Tab tab) {
    	tab.show();
    }
}
