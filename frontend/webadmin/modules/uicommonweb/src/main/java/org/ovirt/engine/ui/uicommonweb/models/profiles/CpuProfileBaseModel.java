package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class CpuProfileBaseModel extends ProfileBaseModel<CpuProfile, CpuQos, Cluster> {

    private static final CpuQos EMPTY_QOS;

    static {
        EMPTY_QOS = new CpuQos();
        EMPTY_QOS.setName(ConstantsManager.getInstance().getConstants().unlimitedQoSTitle());
        EMPTY_QOS.setId(Guid.Empty);
    }

    public CpuProfileBaseModel(IModel sourceModel,
            Guid dcId,
            Guid defaultQosId,
            ActionType actionType) {
        super(sourceModel, dcId, defaultQosId, actionType);
    }

    @Override
    public void flush() {
        if (getProfile() == null) {
            setProfile(new CpuProfile());
        }
        CpuProfile cpuProfile = getProfile();
        cpuProfile.setName(getName().getEntity());
        cpuProfile.setDescription(getDescription().getEntity());
        Cluster cluster = getParentListModel().getSelectedItem();
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
        CpuProfileParameters cpuProfileParameters = new CpuProfileParameters(getProfile());
        cpuProfileParameters.setAddPermissions(true);
        return cpuProfileParameters;
    }
}
