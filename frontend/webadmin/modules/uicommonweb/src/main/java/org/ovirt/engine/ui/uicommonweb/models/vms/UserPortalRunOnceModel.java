package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class UserPortalRunOnceModel extends RunOnceModel {

   public UserPortalRunOnceModel(VM vm, ArrayList<String> customPropertiesKeysList) {
       super(vm, customPropertiesKeysList);
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
   public RunVmOnceParams createRunVmOnceParams() {
       RunVmOnceParams params = super.createRunVmOnceParams();
       // Sysprep params
       if (getSysPrepDomainName().getSelectedItem() != null)
       {
           params.setSysPrepDomainName((String) getSysPrepDomainName().getSelectedItem());
       }

       return params;
   }
}
