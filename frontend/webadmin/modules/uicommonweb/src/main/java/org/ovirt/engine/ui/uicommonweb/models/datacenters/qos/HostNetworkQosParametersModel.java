package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;

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

        getOutAverageLinkshare().setEntity(render(qos.getOutAverageLinkshare()));
        getOutAverageUpperlimit().setEntity(render(qos.getOutAverageUpperlimit()));
        getOutAverageRealtime().setEntity(render(qos.getOutAverageRealtime()));
    }

    /**
     * Returns the String form of an object.
     *
     * @param obj
     *            The object to turn into a String.
     * @return null if the object is null, obj.toString() otherwise.
     */
    private String render(Object obj) {
        return Objects.toString(obj, null);
    }

    @Override
    public void flush(HostNetworkQos qos) {
        qos.setOutAverageLinkshare(parseInteger(getOutAverageLinkshare().getEntity()));
        qos.setOutAverageUpperlimit(parseInteger(getOutAverageUpperlimit().getEntity()));
        qos.setOutAverageRealtime(parseInteger(getOutAverageRealtime().getEntity()));
    }

    /**
     * Returns the equivalent Integer representation of a String, if possible.
     *
     * @param str
     *            The String to try to parse.
     * @return null if the String is null or empty, its Integer value otherwise.
     * @throws NumberFormatException
     *             if the String cannot be parsed as an Integer.
     */
    private Integer parseInteger(String str) {
        return str == null || str.isEmpty() ? null : Integer.parseInt(str);
    }

    protected Collection<IValidation> getOutLinkshareValidations() {
        Collection<IValidation> validations = new ArrayList<>();
        validations.add(new IntegerValidation(1, (Integer) AsyncDataProvider.getInstance()
                .getConfigValuePreConverted(ConfigValues.MaxHostNetworkQosShares)));
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
                        .getConfigValuePreConverted(ConfigValues.MaxAverageNetworkQoSValue)) };
        getOutAverageUpperlimit().validateEntity(rateRangeValidation);
        getOutAverageRealtime().validateEntity(rateRangeValidation);

        setIsValid(getOutAverageLinkshare().getIsValid() && getOutAverageUpperlimit().getIsValid()
                && getOutAverageRealtime().getIsValid());
        return getIsValid();
    }

}
