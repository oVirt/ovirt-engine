package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.List;

import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class CpuProfileBaseModel extends ProfileBaseModel<CpuProfile, CpuQos, VDSGroup> {

    private final static CpuQos EMPTY_QOS;

    static {
        EMPTY_QOS = new CpuQos();
        EMPTY_QOS.setName(ConstantsManager.getInstance().getConstants().unlimitedQoSTitle());
        EMPTY_QOS.setId(Guid.Empty);
    }

    public CpuProfileBaseModel(EntityModel sourceModel,
            Guid dcId,
            Guid defaultQosId,
            VdcActionType vdcActionType) {
        super(sourceModel, dcId, defaultQosId, vdcActionType);
    }

    @Override
    public void flush() {
        if (getProfile() == null) {
            setProfile(new CpuProfile());
        }
        CpuProfile cpuProfile = getProfile();
        cpuProfile.setName(getName().getEntity());
        cpuProfile.setDescription(getDescription().getEntity());
        VDSGroup cluster = getParentListModel().getSelectedItem();
        cpuProfile.setClusterId(cluster != null ? cluster.getId() : null);
        CpuQos cpuQos = getQos().getSelectedItem();
        cpuProfile.setQosId(cpuQos != null
                && cpuQos.getId() != null
                && !cpuQos.getId().equals(Guid.Empty)
                ? cpuQos.getId() : null);
    }

    @Override
    protected void postInitQosList(List<CpuQos> qosList) {
        qosList.add(0, EMPTY_QOS);
        getQos().setItems(qosList);
        if (getDefaultQosId() != null) {
            for (CpuQos cpuQos : qosList) {
                if (getDefaultQosId().equals(cpuQos.getId())) {
                    getQos().setSelectedItem(cpuQos);
                    break;
                }
            }
        }
    }

    @Override
    protected QosType getQosType() {
        return QosType.CPU;
    }

    @Override
    protected ProfileParametersBase<CpuProfile> getParameters() {
        return new CpuProfileParameters(getProfile(), getProfile().getId());
    }
}
