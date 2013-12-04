package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
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
   protected void onRunOnce() {
       startProgress(null);

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
