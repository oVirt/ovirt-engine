package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;

import com.google.inject.Inject;

public class ImportNetworksModel extends BaseImportNetworksModel {

    @Inject
    public ImportNetworksModel(NetworkListModel sourceListModel, DataCenterListModel dataCenterListModel) {
        super(sourceListModel, dataCenterListModel);
    }
}
