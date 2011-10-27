package org.ovirt.engine.ui.userportal.client.views.basic;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.GridElement;
import org.ovirt.engine.ui.userportal.client.views.basic.components.VmTvLayout;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.TileLayoutPolicy;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.tile.TileLayout;

public class VmTileGrid extends GridController<UserPortalItemModel> {

	private TileLayout layout = new TileLayout();
		
	public VmTileGrid() {
		setSelectDefaultValue(true);
		layout.setTileWidth(170);
		layout.setTileHeight(240);
		layout.setAnimateTileChange(false);
		//setTileMargin(0);
		//setExpandMargins(false);
		layout.setLayoutMargin(15);
		layout.setMinWidth(200);
		layout.setShowResizeBar(true);
		layout.setResizeBarTarget("next");
		layout.setTileHMargin(10);
		layout.setTileVMargin(10);
		//layout.setLayoutPolicy(TileLayoutPolicy.FLOW);
	}
	
	@Override
	public Object getId(UserPortalItemModel item) {
		return item.getIsPool() ? ((vm_pools)item.getEntity()).getvm_pool_id() : ((VM)item.getEntity()).getvm_guid();
	}

	@Override
	public GridElement<UserPortalItemModel> addItem(UserPortalItemModel item, int position) {
		final VmTvLayout tile = new VmTvLayout(item);

		// Due to a bug in SmartGWT TileGrid, there is no possibility to add an item at index 0 to a grid already populated with items
		// thus in that case we remove all other items, add the item and add the rest after it (BZ 740213)
		if (position == 0) {
			Canvas tempTile = null;
			ArrayList<Canvas> oldTiles = new ArrayList<Canvas>();
			do {
				tempTile = layout.getTile(0);
				if (tempTile != null) {
					oldTiles.add(tempTile);
					layout.removeTile(tempTile);
				}
			}
			while (tempTile != null);

			layout.addTile(tile);

			for (Canvas c : oldTiles)
				layout.addTile(c);
		}
		else {
			layout.addTile(tile, position);
		}
		
		tile.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				select(tile);
			}
		});
		return tile;
	}

	@Override
	public void removeItem(GridElement<UserPortalItemModel> itemView) {
		layout.removeTile((Canvas)itemView);
	}

	public TileLayout getLayout() {
		return layout;
	}
}
