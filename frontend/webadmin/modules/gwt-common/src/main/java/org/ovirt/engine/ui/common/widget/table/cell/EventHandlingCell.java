package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.view.client.CellPreviewEvent;

public interface EventHandlingCell {

    boolean handlesEvent(CellPreviewEvent<EntityModel> event);

}
