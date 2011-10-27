package org.ovirt.engine.ui.userportal.client.views.extended.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.GridElement;
import org.ovirt.engine.ui.userportal.client.views.extended.templates.components.TemplateGridItem;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class TemplateGrid extends GridController<VmTemplate> {

	private VLayout gridLayout;
	
	public TemplateGrid() {
		gridLayout = new VLayout();
		gridLayout.setOverflow(Overflow.AUTO);
	}
	
	public VLayout getLayout() {
		return gridLayout;
	}
	
	@Override
	public Object getId(VmTemplate item) {
		return item.getId();
	}

	@Override
	public GridElement<VmTemplate> addItem(VmTemplate item, int position) {
		final TemplateGridItem gridItem = new TemplateGridItem(item);
		gridLayout.addMember(gridItem, position);
		gridItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select(gridItem);
			}
		});
		return gridItem;
	}

	@Override
	public void removeItem(GridElement<VmTemplate> itemView) {
	    gridLayout.removeMember((HLayout)itemView);		
	}
}
