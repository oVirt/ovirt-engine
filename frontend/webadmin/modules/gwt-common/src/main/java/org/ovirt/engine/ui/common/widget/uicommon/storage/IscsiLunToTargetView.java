package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class IscsiLunToTargetView extends Composite implements HasEditorDriver<SanStorageModelBase> {

    interface Driver extends UiCommonEditorDriver<SanStorageModelBase, IscsiLunToTargetView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiLunToTargetView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel lunsListPanel;

    SanStorageLunToTargetList sanStorageLunToTargetList;

    private final Driver driver = GWT.create(Driver.class);

    private final double treeHeight;
    private final boolean multiSelection;

    public IscsiLunToTargetView(double treeHeight, boolean multiSelection) {
        this.treeHeight = treeHeight;
        this.multiSelection = multiSelection;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(final SanStorageModelBase object) {
        driver.edit(object);
        initLists(object);
    }

    void initLists(SanStorageModelBase object) {
        sanStorageLunToTargetList = new SanStorageLunToTargetList(object, false, multiSelection);
        sanStorageLunToTargetList.setTreeContainerHeight(treeHeight);
        lunsListPanel.add(sanStorageLunToTargetList);
    }

    @Override
    public SanStorageModelBase flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
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
