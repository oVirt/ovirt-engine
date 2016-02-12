package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;

import com.google.inject.Inject;

public class ImportNetworksModel extends BaseImportNetworksModel {

    @Inject
    public ImportNetworksModel(final com.google.inject.Provider<CommonModel> commonModelProvider,
            NetworkListModel sourceListModel) {
        super(sourceListModel, commonModelProvider);
    }
}
