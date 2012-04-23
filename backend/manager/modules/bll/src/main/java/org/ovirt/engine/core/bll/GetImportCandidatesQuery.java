package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.queries.GetImportCandidatesQueryParameters;
import org.ovirt.engine.core.common.queries.ImportCandidateInfoBase;
import org.ovirt.engine.core.common.queries.ImportCandidateSourceEnum;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImportCandidatesVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

/**
 * Gets a list of all the names (in VmWare's case : the IDs) of all candidates.
 */
public class GetImportCandidatesQuery<P extends GetImportCandidatesQueryParameters>
        extends GetImportCandidatesBase<P> {
    public GetImportCandidatesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getCandidateSource() == ImportCandidateSourceEnum.KVM) {
            // Get the dictionary of candidates and build a list of their names:
            Map<String, ImportCandidateInfoBase> retFromIrs = (Map) Backend
                    .getInstance()
                    .runInternalQuery(
                            VdcQueryType.GetImportCandidatesInfo,
                            new GetImportCandidatesQueryParameters(getParameters().getPath(),
                                    getParameters().getCandidateSource(), getParameters()
                                            .getCandidateType())).getReturnValue();

            if (retFromIrs == null) {
                getQueryReturnValue().setReturnValue(null);
            } else {
                List<String> list = LinqUtils.foreach(retFromIrs.values(),
                        new Function<ImportCandidateInfoBase, String>() {
                            @Override
                            public String eval(ImportCandidateInfoBase importCandidateInfoBase) {
                                return importCandidateInfoBase.getCandidateDisplayName();
                            }
                        });
                getQueryReturnValue().setReturnValue(list);
            }
        }

        else // VMWARE -> get the list of candidates' IDs straight from the
             // resource manager:
        {
            // todo - omer handle this
            java.util.ArrayList<String> retFromIrs = (java.util.ArrayList<String>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImportCandidates,
                            new GetImportCandidatesVDSCommandParameters(Guid.Empty, getParameters().getPath(),
                                    getParameters().getCandidateSource(), getParameters()
                                            .getCandidateType())).getReturnValue();

            getQueryReturnValue().setReturnValue(retFromIrs);
        }
    }
}
