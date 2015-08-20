package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class UserPortalRunOnceModel extends RunOnceModel {

   public UserPortalRunOnceModel(VM vm, ICommandTarget commandTarget) {
       super(vm, commandTarget);
   }

   @Override
   public void init() {
       super.init();

       // disable Host tab
       setIsHostTabVisible(false);
       // disable Custom properties sheet
       setIsCustomPropertiesSheetVisible(false);
   }

   @Override
   protected void onRunOnce() {
       startProgress();

       Frontend.getInstance().runAction(VdcActionType.RunVmOnce, createRunVmOnceParams(),
               new IFrontendActionAsyncCallback() {
                   @Override
                   public void executed(FrontendActionAsyncResult result) {
                       stopProgress();
                       commandTarget.executeCommand(runOnceCommand);
                   }
               }, this);
   }
}
