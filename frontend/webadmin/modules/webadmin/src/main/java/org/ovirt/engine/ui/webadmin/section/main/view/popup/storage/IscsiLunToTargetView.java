package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.webadmin.widget.storage.SanStorageLunToTargetList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class IscsiLunToTargetView extends Composite implements HasEditorDriver<SanStorageModelBase> {

    interface Driver extends SimpleBeanEditorDriver<SanStorageModelBase, IscsiLunToTargetView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiLunToTargetView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel lunsListPanel;

    SanStorageLunToTargetList sanStorageLunToTargetList;

    public IscsiLunToTargetView() {

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final SanStorageModelBase object) {
        Driver.driver.edit(object);
        initLists(object);
    }

    void initLists(SanStorageModelBase object) {
        sanStorageLunToTargetList = new SanStorageLunToTargetList(object);
        lunsListPanel.add(sanStorageLunToTargetList);
    }

    @Override
    public SanStorageModelBase flush() {
        return Driver.driver.flush();
    }

    public void activateItemsUpdate() {
        sanStorageLunToTargetList.activateItemsUpdate();
    }

    public void disableItemsUpdate() {
        sanStorageLunToTargetList.disableItemsUpdate();
    }

    public void setTreeContainerStyleName(String expandedlunsListPanel) {
        sanStorageLunToTargetList.setTreeContainerStyleName(expandedlunsListPanel);
    }

}
