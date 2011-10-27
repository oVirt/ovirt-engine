package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Mapper;

import java.util.Map;

/**
 * Gets info on a specified import candidate.
 */
public class GetCandidateInfoQuery<P extends CandidateInfoParameters> extends GetImportCandidatesBase<P> {
    public GetCandidateInfoQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        ImportCandidateInfoBase returnVal = null;
        if (!getParameters().getIsName()
                || getParameters().getCandidateSource() == ImportCandidateSourceEnum.VMWARE) {
            // Get the spcified candidate's info from the resource manager:
            // todo - omer handle this
            returnVal = (ImportCandidateInfoBase) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetCandidateInfo,
                            new GetCandidateInfoVDSCommandParameters(Guid.Empty, getParameters()
                                    .getCandidateIdOrName(), getParameters().getPath(),
                                    getParameters().getCandidateSource(), getParameters()
                                            .getCandidateType())).getReturnValue();
        }

        else {
            // parameter is name and candidate source is KVM -> get all
            // candidates info and look for the one with the matching name:
            final Map<String, ImportCandidateInfoBase> retFromIrs = (Map) Backend
                    .getInstance()
                    .runInternalQuery(
                            VdcQueryType.GetImportCandidatesInfo,
                            new GetImportCandidatesQueryParameters(getParameters().getPath(),
                                    getParameters().getCandidateSource(), getParameters()
                                            .getCandidateType())).getReturnValue();

            try {
                // LINQ 29456
                // Dictionary<string, ImportCandidateInfoBase> dictByName =
                // retFromIrs.ToDictionary
                // <KeyValuePair<string, ImportCandidateInfoBase>, string,
                // ImportCandidateInfoBase>
                // (a => a.Value.CandidateDisplayName, a => a.Value);

                Map<String, ImportCandidateInfoBase> dictByName = LinqUtils.toMap(retFromIrs.keySet(),
                        new Mapper<String, String, ImportCandidateInfoBase>() {
                            @Override
                            public String createKey(String key) {
                                return retFromIrs.get(key).getCandidateDisplayName();
                            }

                            @Override
                            public ImportCandidateInfoBase createValue(String key) {
                                return retFromIrs.get(key);
                            }
                        });

                // if
                // (dictByName.ContainsKey(CandidateInfoParameters.CandidateIdOrName))
                // {
                // returnVal =
                // dictByName[CandidateInfoParameters.CandidateIdOrName];
                // }

                if (dictByName.containsKey(getParameters().getCandidateIdOrName())) {
                    returnVal = dictByName.get(getParameters().getCandidateIdOrName());
                }
            }

            catch (IllegalArgumentException ae) {
                // there are several candidates with the same name ->
                // look for the candidate by name by searching one by one:
                log.warnFormat(
                        "GetCandidateInfoQuery::ExecuteQueryCommand: There are several import candidates (path: {0}, source {1}, type {2}) with the same name.",
                        getParameters().getPath(),
                        getParameters().getCandidateSource(),
                        getParameters().getCandidateType());

                for (String candidateID : retFromIrs.keySet()) {
                    if (StringHelper.EqOp(retFromIrs.get(candidateID).getCandidateDisplayName(),
                            getParameters().getCandidateIdOrName())) {
                        returnVal = retFromIrs.get(candidateID);
                        break;
                    }
                }
            }
        }

        if (returnVal == null) {
            String errorString = String.format("GetCandidateInfoQuery: Could not find candidate '%1$s'.",
                    getParameters().getCandidateIdOrName());

            log.warn(errorString);
        }

        if (!(returnVal instanceof VmCandidateInfo)
                && getParameters().getCandidateType() == ImportCandidateTypeEnum.VM) {
            String errorString =
                    "GetCandidateInfoQuery: Info was request for a Desktop, but a non-Desktop identifier was supplied.";

            log.warn(errorString);
            returnVal = null;
        }

        if (!(returnVal instanceof TemplateCandidateInfo)
                && getParameters().getCandidateType() == ImportCandidateTypeEnum.TEMPLATE) {
            String errorString =
                    "GetCandidateInfoQuery: Info was requested for a Template, but a non-Template identifier was supplied.";

            log.warn(errorString);
            returnVal = null;
        }
        getQueryReturnValue().setReturnValue(returnVal);
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetCandidateInfoQuery.class);
}
