package org.ovirt.engine.core.common.businessentities.network;

/**
 * {@code TlvSpecificType} defines organization specific TLVs
 */
public enum TlvSpecificType {
    PortDescription(4),
    SystemName(5),
    PortVlanID(127, 0x0080c2, 1),
    PortAndProtocolVlanID(127, 0x0080c2, 2),
    VlanName(127, 0x0080c2, 3),
    LinkAggregation802_1(127, 0x0080c2, 7),
    LinkAggregation802_3(127, 0x00120f, 3),
    MaximumFrameSize(127, 0x00120f, 4);

    private int type;
    private Integer oui;
    private Integer subType;

    private TlvSpecificType(int type, int oui, int subType) {
        this.type = type;
        this.oui = oui;
        this.subType = subType;
    }

    private TlvSpecificType(int type) {
        this.type = type;
    }

    public int getSubType() {
        return subType;
    }

    public int getType() {
        return type;
    }

    public int getOui() {
        return oui;
    }

    public boolean isSameAsTlv(Tlv tlv){
        if (type < 127) {
            return type == tlv.getType();
        } else if (type == 127) {
            return oui.equals(tlv.getOui()) && subType.equals(tlv.getSubtype());
        }
        return false;
    }
}
