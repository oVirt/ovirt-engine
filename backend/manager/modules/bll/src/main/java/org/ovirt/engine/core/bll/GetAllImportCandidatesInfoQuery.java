package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.queries.*;
import java.util.Map;

public class GetAllImportCandidatesInfoQuery<P extends GetAllImportCandidatesQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAllImportCandidatesInfoQuery(P baseParams) {
        super(baseParams);
    }

    @Override
    protected void executeQueryCommand() {
        Map<ImportCandidateSourceEnum, Map<String, ImportCandidateInfoBase>> ret =
                new java.util.HashMap<ImportCandidateSourceEnum, Map<String, ImportCandidateInfoBase>>();

        for (ImportCandidateSourceEnum source : EnumCompat.GetValues(ImportCandidateSourceEnum.class)) {
            if (getParameters().getCandidateType() == ImportCandidateTypeEnum.TEMPLATE
                    && source == ImportCandidateSourceEnum.VMWARE) {
                continue; // No such thing as VmWare Templates.
            }
            Map<String, ImportCandidateInfoBase> dict = (Map) Backend
                    .getInstance()
                    .runInternalQuery(
                            VdcQueryType.GetImportCandidatesInfo,
                            new GetImportCandidatesQueryParameters(getParameters().getPath(), source,
                                    getParameters().getCandidateType())).getReturnValue();

            if (dict != null) {
                ret.put(source, dict);
            }
        }

        getQueryReturnValue().setReturnValue(ret);
    }
}
