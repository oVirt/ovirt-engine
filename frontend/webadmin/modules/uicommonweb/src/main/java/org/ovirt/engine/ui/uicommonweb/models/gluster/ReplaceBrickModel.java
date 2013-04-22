package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ReplaceBrickModel extends Model {

    ListModel servers;
    EntityModel brickDirectory;

    public ReplaceBrickModel()
    {
        setServers(new ListModel());
        setBrickDirectory(new EntityModel());
    }

    public ListModel getServers() {
        return servers;
    }

    public void setServers(ListModel servers) {
        this.servers = servers;
    }

    public EntityModel getBrickDirectory() {
        return brickDirectory;
    }

    public void setBrickDirectory(EntityModel brickDirectory) {
        this.brickDirectory = brickDirectory;
    }

    public boolean validate()
    {
        getBrickDirectory().validateEntity(new IValidation[] { new NotEmptyValidation() });

        return getServers().getIsValid() && getBrickDirectory().getIsValid();
    }

}
