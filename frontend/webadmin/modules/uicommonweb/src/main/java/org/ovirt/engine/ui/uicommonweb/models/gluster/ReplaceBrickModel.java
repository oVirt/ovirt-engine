package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ReplaceBrickModel extends Model {

    ListModel<VDS> servers;
    EntityModel<String> brickDirectory;

    public ReplaceBrickModel() {
        setServers(new ListModel<VDS>());
        setBrickDirectory(new EntityModel<String>());
    }

    public ListModel<VDS> getServers() {
        return servers;
    }

    public void setServers(ListModel<VDS> servers) {
        this.servers = servers;
    }

    public EntityModel<String> getBrickDirectory() {
        return brickDirectory;
    }

    public void setBrickDirectory(EntityModel<String> brickDirectory) {
        this.brickDirectory = brickDirectory;
    }

    public boolean validate() {
        getBrickDirectory().validateEntity(new IValidation[] { new NotEmptyValidation() });

        return getServers().getIsValid() && getBrickDirectory().getIsValid();
    }

}
