package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class UserPortalRunOnceModel extends RunOnceModel {

   public UserPortalRunOnceModel(VM vm, ArrayList<String> customPropertiesKeysList, ICommandTarget commandTarget) {
       super(vm, customPropertiesKeysList, commandTarget);
   }

   @Override
   public void init() {
       super.init();

       // disable Host tab
       setIsHostTabVisible(false);
       // disable Custom properties sheet
       setIsCustomPropertiesSheetVisible(false);

       getCustomProperties().setEntity(vm.getCustomProperties());
   }

   @Override
   protected RunVmOnceParams createRunVmOnceParams() {
       RunVmOnceParams params = super.createRunVmOnceParams();
       // Sysprep params
       if (getSysPrepDomainName().getSelectedItem() != null)
       {
           params.setSysPrepDomainName((String) getSysPrepDomainName().getSelectedItem());
       }

       return params;
   }

   @Override
   protected void onRunOnce() {
       startProgress(null);

       Frontend.RunAction(VdcActionType.RunVmOnce, createRunVmOnceParams(),
               new IFrontendActionAsyncCallback() {
                   @Override
                   public void executed(FrontendActionAsyncResult result) {
                       stopProgress();
                       commandTarget.executeCommand(runOnceCommand);
                   }
               }, this);
   }
}
