package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.queries.*;

public final class ImportEnumsManager {
    private static java.util.HashMap<ImportCandidateSourceEnum, String> CandidateSourceEnumStrings;
    private static java.util.HashMap<ImportCandidateTypeEnum, String> CandidateTypeEnumStrings;

    static {
        CandidateSourceEnumStrings = new java.util.HashMap<ImportCandidateSourceEnum, String>();
        CandidateSourceEnumStrings.put(ImportCandidateSourceEnum.KVM, "qumranet");
        CandidateSourceEnumStrings.put(ImportCandidateSourceEnum.VMWARE, "vmware");

        CandidateTypeEnumStrings = new java.util.HashMap<ImportCandidateTypeEnum, String>();
        CandidateTypeEnumStrings.put(ImportCandidateTypeEnum.VM, "vms");
        CandidateTypeEnumStrings.put(ImportCandidateTypeEnum.TEMPLATE, "templates");
    }

    public static String CandidateSourceString(ImportCandidateSourceEnum source) {
        return CandidateSourceEnumStrings.get(source);
    }

    public static String CandidateTypeString(ImportCandidateTypeEnum type) {
        return CandidateTypeEnumStrings.get(type);
    }
}
