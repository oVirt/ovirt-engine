package org.ovirt.engine.ui.userportal.client.views.extended.templates.components;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.EntityModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.client.binders.SubTabView;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.Tab;
import org.ovirt.engine.ui.userportal.client.components.TabPanel;

public class TemplateItemSubTabs extends TabPanel {

	private Map<EntityModel,DetailTab> tabsToModels = new HashMap<EntityModel, DetailTab>();
	
	UserPortalTemplateListModel tlm;
	
	public TemplateItemSubTabs(final UserPortalTemplateListModel tlm, final GridController gridController) {
		setHeight(180);
		hide();
		
		this.tlm = tlm;
		
		tlm.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs pargs = (PropertyChangedEventArgs)args;
				if (pargs.PropertyName.equals("DetailModels")) {
					show();
					for (EntityModel model : tlm.getDetailModels()) {
						if (!(model.getTitle().equals("Storage") || model.getTitle().equals("Virtual Machines")))
							addTab(new DetailTab(new SubTabView(model, gridController), model));
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
			tlm.setActiveDetailModel(model);
		}
	}
}