package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;

//class CanDoActionQuery : QueriesCommandBase
//{
//    internal CanDoActionQuery(CanDoActionParameters parameters)
//        : base(parameters)
//    {

//    }

//    private CanDoActionParameters _params
//    {
//        get
//        {
//            return Parameters as CanDoActionParameters;
//        }
//    }

//    protected override void ExecuteQueryCommand()
//    {
//        List<string> reasons;
//        QueryReturnValue.ReturnValue = CommandsFactory.canDoActionWithParameters(_params.Action, _params.Id, out reasons, null);
//        (QueryReturnValue as VdcCanDoQueryReturnValue).Messages = reasons;
//    }
//}

//class CanDoActionWithParametersQuery : QueriesCommandBase
//{
//    internal CanDoActionWithParametersQuery(CanDoActionWithParametersParameters parameters)
//        : base(parameters)
//    {
//    }

//    private CanDoActionWithParametersParameters _params
//    {
//        get
//        {
//            return Parameters as CanDoActionWithParametersParameters;
//        }
//    }

//    protected override void ExecuteQueryCommand()
//    {
//        List<string> reasons;
//        QueryReturnValue.ReturnValue = CommandsFactory.canDoActionWithParameters(_params.Action, _params.Id, out reasons, _params.AdditionalParameters);
//        (QueryReturnValue as VdcCanDoQueryReturnValue).Messages = reasons;
//    }
//}

public class GetAllServerCpuListQuery<P extends GetAllServerCpuListParameters> extends QueriesCommandBase<P> {
    public GetAllServerCpuListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(CpuFlagsManagerHandler.allServerCpuList(getParameters().getVersion()));
    }
}
