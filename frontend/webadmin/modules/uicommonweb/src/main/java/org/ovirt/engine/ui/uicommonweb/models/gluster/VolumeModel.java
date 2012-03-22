package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class VolumeModel extends Model {
    EntityModel name;
    ListModel typeList;
    EntityModel bricks;
    EntityModel gluster_accecssProtocol;
    EntityModel nfs_accecssProtocol;
    EntityModel cifs_accecssProtocol;
    EntityModel users;
    EntityModel allowAccess;

    public VolumeModel() {
        setName(new EntityModel());

        setTypeList(new ListModel());
        ArrayList<GlusterVolumeType> list =
                new ArrayList<GlusterVolumeType>(Arrays.asList(new GlusterVolumeType[] { GlusterVolumeType.DISTRIBUTE,
                        GlusterVolumeType.REPLICATE, GlusterVolumeType.STRIPE }));
        getTypeList().setItems(list);
        getTypeList().setSelectedItem(GlusterVolumeType.DISTRIBUTE);

        setBricks(new EntityModel());

        setGluster_accecssProtocol(new EntityModel());
        getGluster_accecssProtocol().setEntity(true);
        getGluster_accecssProtocol().setIsChangable(false);

        setNfs_accecssProtocol(new EntityModel());
        getNfs_accecssProtocol().setEntity(true);

        setCifs_accecssProtocol(new EntityModel());
        getCifs_accecssProtocol().setEntity(false);

        setUsers(new EntityModel());

        setAllowAccess(new EntityModel());
        getAllowAccess().setEntity("*");
    }

    public EntityModel getName() {
        return name;
    }

    public void setName(EntityModel name) {
        this.name = name;
    }

    public ListModel getTypeList() {
        return typeList;
    }

    public void setTypeList(ListModel typeList) {
        this.typeList = typeList;
    }

    public EntityModel getBricks() {
        return bricks;
    }

    public void setBricks(EntityModel bricks) {
        this.bricks = bricks;
    }

    public EntityModel getGluster_accecssProtocol() {
        return gluster_accecssProtocol;
    }

    public void setGluster_accecssProtocol(EntityModel gluster_accecssProtocol) {
        this.gluster_accecssProtocol = gluster_accecssProtocol;
    }

    public EntityModel getNfs_accecssProtocol() {
        return nfs_accecssProtocol;
    }

    public void setNfs_accecssProtocol(EntityModel nfs_accecssProtocol) {
        this.nfs_accecssProtocol = nfs_accecssProtocol;
    }

    public EntityModel getCifs_accecssProtocol() {
        return cifs_accecssProtocol;
    }

    public void setCifs_accecssProtocol(EntityModel cifs_accecssProtocol) {
        this.cifs_accecssProtocol = cifs_accecssProtocol;
    }

    public EntityModel getUsers() {
        return users;
    }

    public void setUsers(EntityModel users) {
        this.users = users;
    }

    public EntityModel getAllowAccess() {
        return allowAccess;
    }

    public void setAllowAccess(EntityModel allowAccess) {
        this.allowAccess = allowAccess;
    }

    public boolean Validate() {
        return true;
    }
}
