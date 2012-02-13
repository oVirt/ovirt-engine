package org.ovirt.engine.ui.userportal.client.views.extended.maingrid;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.GridElement;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.components.MainGridItem;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class UserPortalItemsGrid extends GridController<UserPortalItemModel> {

	public VLayout gridLayout;
	private MainGrid parent;
	public UserPortalItemsGrid(MainGrid parent) {
		this.parent = parent;
		gridLayout = new VLayout();
		gridLayout.setOverflow(Overflow.AUTO);
		gridLayout.setResizeBarTarget("next");
	}

	public VLayout getLayout() {
		return gridLayout;
	}

	@Override
	public Object getId(UserPortalItemModel item) {
		return item.getIsPool() ? ((vm_pools)item.getEntity()).getvm_pool_id() : ((VM)item.getEntity()).getId();
	}

	@Override
	public GridElement<UserPortalItemModel> addItem(UserPortalItemModel item, int position) {
		GWT.log("Adding " + item.getName());
		final MainGridItem gridItem = new MainGridItem(this, item);
		gridItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select(gridItem);
			}
		});

		gridLayout.addMember(gridItem, position);
		return gridItem;
	}

	@Override
	public void removeItem(GridElement<UserPortalItemModel> itemView) {
		gridLayout.removeMember((HLayout)itemView);
	}

	public void keyPressed(String keyName) {
		if (keyName.equals("Arrow_Up")) {
			if (selectedElement != null) {
				int selectedElementPos = gridLayout.getMemberNumber((Canvas)selectedElement);
				if (selectedElementPos > 0)
					select((GridElement<UserPortalItemModel>)gridLayout.getMember(selectedElementPos - 1));
			}
		}
		if (keyName.equals("Arrow_Down")) {
			if (selectedElement != null) {
				int selectedElementPos = gridLayout.getMemberNumber((Canvas)selectedElement);
				if (selectedElementPos < gridLayout.getMembers().length-1)
					select((GridElement<UserPortalItemModel>)gridLayout.getMember(selectedElementPos + 1));
			}
		}
	}

	public MainGrid getMainGrid() {
		return parent;
	}
}
