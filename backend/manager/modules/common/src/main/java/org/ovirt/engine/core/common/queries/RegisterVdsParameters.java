package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RegisterVdsParameters")
public class RegisterVdsParameters extends VdcQueryParametersBase implements Serializable {
    private static final long serialVersionUID = 4661626618754048420L;

    @XmlElement
    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    private void setVdsId(Guid value) {
        privateVdsId = value;
    }

    @XmlElement
    private String privateVdsHostName;

    public String getVdsHostName() {
        return privateVdsHostName;
    }

    private void setVdsHostName(String value) {
        privateVdsHostName = value;
    }

    @XmlElement
    private String privateVdsName;

    public String getVdsName() {
        return privateVdsName;
    }

    public void setVdsName(String value) {
        privateVdsName = value;
    }

    @XmlElement
    private String privateVdsUniqueId;

    public String getVdsUniqueId() {
        return privateVdsUniqueId;
    }

    private void setVdsUniqueId(String value) {
        privateVdsUniqueId = value;
    }

    @Deprecated
    @XmlElement
    private String privateNetMask;

    public String getNetMask() {
        return privateNetMask;
    }

    @Deprecated
    public void setNetMask(String value) {
        privateNetMask = value;
    }

    @XmlElement
    private int privatePort;

    public int getPort() {
        return privatePort;
    }

    private void setPort(int value) {
        privatePort = value;
    }

    @XmlElement
    private Guid privateVdsGroupId;

    public Guid getVdsGroupId() {
        return privateVdsGroupId;
    }

    private void setVdsGroupId(Guid value) {
        privateVdsGroupId = value;
    }

    @XmlElement
    private VDSType privateVdsType = VDSType.forValue(0);

    public VDSType getVdsType() {
        return privateVdsType;
    }

    private void setVdsType(VDSType value) {
        privateVdsType = value;
    }

    private Long otp;

    public Long getOtp() {
        return otp;
    }

    public void setOtp(Long otp) {
        this.otp = otp;
    }

    public RegisterVdsParameters(Guid vdsId, String vds_host_name, String vds_name, String vds_unique_id, int port,
            String netmask, Guid vds_group_id, VDSType vds_type) {
        setVdsId(vdsId);
        setVdsHostName(vds_host_name);
        setVdsName(vds_name);
        setVdsUniqueId(vds_unique_id);
        setPort(port);
        setNetMask(netmask);
        setVdsGroupId(vds_group_id);
        setVdsType(vds_type);
    }

    public RegisterVdsParameters() {
    }
}
