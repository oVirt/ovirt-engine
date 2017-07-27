package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.network.LldpInfo;
import org.ovirt.engine.core.common.businessentities.network.Tlv;
import org.ovirt.engine.core.common.vdscommands.GetLldpVDSCommandParameters;

public class GetLldpVDSCommand<T extends GetLldpVDSCommandParameters> extends VdsBrokerCommand<T> {

    private LldpReturn lldpReturn;

    public GetLldpVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        lldpReturn = getBroker().getLldp(getParameters().getInterfaces());
        proceedProxyReturnValue();
        setReturnValue(parseLldpInfos(lldpReturn.getLldp()));
    }

    private Map<String, LldpInfo> parseLldpInfos(Map<String, Object> lldps) {
        return lldps.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> parseLldpInfo((Map<String, Object>)entry.getValue())));
    }

    private LldpInfo parseLldpInfo(Map<String, Object> lldp) {
        LldpInfo lldpInfo = new LldpInfo();
        lldpInfo.setEnabled((boolean)lldp.get(VdsProperties.LLDP_ENABLED));
        lldpInfo.setTlvs(parseTlvs((Object[])lldp.get(VdsProperties.LLDP_TLVS)));
        return lldpInfo;
    }

    private List<Tlv> parseTlvs(Object[] tlvs) {
        return Arrays.stream(tlvs).map(tlv -> (Map<String, Object>) tlv).map(this::parseTlv)
                .collect(Collectors.toList());
    }

    private Tlv parseTlv(Map<String, Object> in) {
        Tlv out = new Tlv();
        out.setName((String)(in.get(VdsProperties.TLV_NAME)));
        out.setType((Integer)(in.get(VdsProperties.TLV_TYPE)));
        if (in.containsKey(VdsProperties.TLV_OUI)) {
            out.setOui((Integer)(in.get(VdsProperties.TLV_OUI)));
        }
        if (in.containsKey(VdsProperties.TLV_SUBTYPE)) {
            out.setSubtype((Integer)(in.get(VdsProperties.TLV_SUBTYPE)));
        }
        Map<String, String> entries = (Map<String, String>) in.get(VdsProperties.TLV_PROPERTIES);
        out.getProperties().putAll(entries);

        return out;
    }

    @Override
    protected Status getReturnStatus() {
        return lldpReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return lldpReturn;
    }
}
