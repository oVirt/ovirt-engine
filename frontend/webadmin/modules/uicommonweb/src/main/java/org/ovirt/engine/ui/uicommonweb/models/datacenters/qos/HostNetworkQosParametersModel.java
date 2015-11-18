package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class HostNetworkQosParametersModel extends QosParametersModel<HostNetworkQos> {

    private EntityModel<String> outAverageLinkshare = new EntityModel<>();
    private EntityModel<String> outAverageUpperlimit = new EntityModel<>();
    private EntityModel<String> outAverageRealtime = new EntityModel<>();

    public EntityModel<String> getOutAverageLinkshare() {
        return outAverageLinkshare;
    }

    public EntityModel<String> getOutAverageUpperlimit() {
        return outAverageUpperlimit;
    }

    public EntityModel<String> getOutAverageRealtime() {
        return outAverageRealtime;
    }

    @Override
    public void init(HostNetworkQos qos) {
        if (qos == null) {
            qos = new HostNetworkQos();
        }

        getOutAverageLinkshare().setEntity(StringUtils.render(qos.getOutAverageLinkshare()));
        getOutAverageUpperlimit().setEntity(StringUtils.render(qos.getOutAverageUpperlimit()));
        getOutAverageRealtime().setEntity(StringUtils.render(qos.getOutAverageRealtime()));
    }

    @Override
    public void flush(HostNetworkQos qos) {
        qos.setOutAverageLinkshare(StringUtils.parseInteger(getOutAverageLinkshare().getEntity()));
        qos.setOutAverageUpperlimit(StringUtils.parseInteger(getOutAverageUpperlimit().getEntity()));
        qos.setOutAverageRealtime(StringUtils.parseInteger(getOutAverageRealtime().getEntity()));
    }

    protected Collection<IValidation> getOutLinkshareValidations() {
        Collection<IValidation> validations = new ArrayList<>();
        validations.add(new IntegerValidation(1, (Integer) AsyncDataProvider.getInstance()
                .getConfigValuePreConverted(ConfigurationValues.MaxHostNetworkQosShares)));
        return validations;
    }

    @Override
    public boolean validate() {
        if (!getIsChangable() || !getIsAvailable()) {
            return true;
        }

        Collection<IValidation> outLinkshareValidations = getOutLinkshareValidations();
        getOutAverageLinkshare().validateEntity(outLinkshareValidations.toArray(new IValidation[outLinkshareValidations.size()]));

        IValidation[] rateRangeValidation =
                new IValidation[] { new IntegerValidation(1, (Integer) AsyncDataProvider.getInstance()
                        .getConfigValuePreConverted(ConfigurationValues.MaxAverageNetworkQoSValue)) };
        getOutAverageUpperlimit().validateEntity(rateRangeValidation);
        getOutAverageRealtime().validateEntity(rateRangeValidation);

        setIsValid(getOutAverageLinkshare().getIsValid() && getOutAverageUpperlimit().getIsValid()
                && getOutAverageRealtime().getIsValid());
        return getIsValid();
    }

}
