package org.ovirt.engine.ui.userportal.client.modalpanels;

import org.ovirt.engine.ui.uicommon.UICommand;
import org.ovirt.engine.ui.uicommon.models.userportal.AttachCdModel;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import org.ovirt.engine.ui.userportal.client.components.Button;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.MainGrid;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.UserPortalItemsGrid;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.HLayout;

public class ChangeCdModalPanel extends NonDraggableModalPanel {
    final ChangeCdModalPanel attachCDModalPanel = this;
    final MainGrid mainGrid;

    public ChangeCdModalPanel(String title, MainGrid mainGrid) {
		super(300, 120, title);

		this.mainGrid = mainGrid;

		final AttachCdModel attachCdModel = mainGrid.uplm.getAttachCdModel();

		// Creating and set a inner panel
		HLayout inPanel = new HLayout();
		inPanel.setHeight100();
		inPanel.setWidth100();
		inPanel.setAlign(Alignment.CENTER);

		// Create CD images selectbox
		SelectBoxListModelBinded cdImageBox = new SelectBoxListModelBinded("Attach CD", attachCdModel.getIsoImage(), String.class);
		cdImageBox.setDisabled(!attachCdModel.getIsoImage().getIsChangable());
		cdImageBox.setShowTitle(false);
		cdImageBox.setWidth(240);

		/** Add components to panel **/

		// Set items inside a form
		DynamicForm f = new DynamicForm();
		f.setItems(cdImageBox);

		// Adding components to inner panel
		inPanel.addMember(f);

		// Adding inner panel to this view
		addItem(inPanel);

		// Add footer buttons
		addFooterButtons();
    }

	private void addFooterButtons() {
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {

			@Override
		    public void onClick(ClickEvent event) {
				mainGrid.uplm.ExecuteCommand(new UICommand("OnChangeCD", null));
			    attachCDModalPanel.destroy();
			    mainGrid.gridActionPerformed();
		    }
		});
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
		    @Override
		    public void onClick(ClickEvent event) {
		    	attachCDModalPanel.destroy();
		    }
		});

		setFooterButtons(Alignment.RIGHT, okButton, cancelButton);
	}
}