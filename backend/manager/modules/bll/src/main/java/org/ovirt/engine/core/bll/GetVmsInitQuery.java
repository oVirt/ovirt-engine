package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdsQueryParameters;

/**
 *  Query set VmInit to VMs *** OR *** Templates
 */
public class GetVmsInitQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {
   public GetVmsInitQuery(P parameters) {
       super(parameters);
   }

   @Override
   protected void executeQueryCommand() {
       if (getParameters().getIds() != null) {
           getQueryReturnValue().setReturnValue(VmHandler.getVmInitByIds(getParameters().getIds(), true));
       }
   }
}
