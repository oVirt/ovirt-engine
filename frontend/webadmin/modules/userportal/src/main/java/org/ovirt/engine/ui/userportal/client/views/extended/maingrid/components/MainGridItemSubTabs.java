package org.ovirt.engine.ui.userportal.client.views.extended.maingrid.components;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.client.binders.SubTabView;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.Tab;
import org.ovirt.engine.ui.userportal.client.components.TabPanel;

import com.google.gwt.core.client.GWT;

public class MainGridItemSubTabs extends TabPanel {
	private Map<EntityModel,DetailTab> tabsToModels = new HashMap<EntityModel, DetailTab>();
	
	UserPortalListModel uplm;
	
	public MainGridItemSubTabs(final UserPortalListModel uplm, final GridController gridController) {
		setHeight(180);
		hide();
		
		this.uplm = uplm;
		
		uplm.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs pargs = (PropertyChangedEventArgs)args;
				if (pargs.PropertyName.equals("DetailModels")) {
					show();
					for (final EntityModel model : uplm.getDetailModels()) {
						final DetailTab tab = new DetailTab(new SubTabView(model, gridController), model);
						addTab(tab, !model.getIsAvailable());						
						model.getPropertyChangedEvent().addListener(new IEventListener() {
							@Override
							public void eventRaised(Event ev, Object sender, EventArgs args) {
								if (((PropertyChangedEventArgs)args).PropertyName.equals("IsAvailable")) {
									if (model.getIsAvailable())
										showTab(tab);
									else 
										hideTab(tab);
								}
							}
						});
					}
				}
				else if (pargs.PropertyName.equals("ActiveDetailModel")) {
					if (!tabsToModels.get(uplm.getActiveDetailModel()).equals(getSelectedTab())) {
						GWT.log("Main grid active detail tab changed to " + uplm.getActiveDetailModel().getTitle() + " by UICommon");
						select(tabsToModels.get(uplm.getActiveDetailModel()));
					}
				}
			}
		});
	}
		
	class DetailTab extends Tab {
		EntityModel model;

		public DetailTab(SubTabView subTabView, EntityModel model) {
			super();
			setTitle(model.getTitle());
			tabsToModels.put(model, this);
			this.model = model;
			setPane(subTabView.getLayout());
		}
		
		@Override
		public void select() {
			super.select();
			uplm.setActiveDetailModel(model);
		}
	}
}
