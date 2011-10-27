package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Function;

import java.util.List;
import java.util.Map;

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
                // LINQ 29456
                // QueryReturnValue.ReturnValue = new List<string>
                // (retFromIrs.Values.Select<ImportCandidateInfoBase,string>
                // (a => a.CandidateDisplayName));

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
