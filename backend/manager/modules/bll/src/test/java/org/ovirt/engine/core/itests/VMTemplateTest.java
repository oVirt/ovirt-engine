package org.ovirt.engine.core.itests;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;

public class VMTemplateTest extends AbstractBackendTest {

    /**
     * Test adding a template for public use to the system. Such a template must create permissions on user EVERYONE as
     * role TEMPLATE_USER
     *
     * result - success when commands getSuccedded() returns true and the expected permissions exist in the DB.
     */
    @Test
    public void addTemplate() {

        BasicTestSetup basicSetup = getBasicSetup();
        AddVmTemplateParameters parameters = new AddVmTemplateParameters(basicSetup.getVm().getStaticData(), "template"
                + testSequence, "test template");
        parameters.setPublicUse(true);

        VdcReturnValueBase runAction = backend.runInternalAction(VdcActionType.AddVmTemplate, sessionize(parameters));
        Guid vmtGuid = (Guid) runAction.getActionReturnValue();
        VdcQueryReturnValue runQuery = backend.RunQuery(VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(vmtGuid));
        ArrayList<permissions> perms = (ArrayList<permissions>) runQuery.getReturnValue();
        // evaluate results
        Assert.assertTrue(runAction.getSucceeded() && !perms.isEmpty()
                && perms.get(0).getad_element_id().equals(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID));
        // clean the created template and images
        cleanup(basicSetup, vmtGuid);
    }

    private void cleanup(BasicTestSetup basicSetup, Guid vmtGuid) {
        runAsSuperAdmin(); // run as admin so we can erase the template
        VmTemplateParametersBase removeImagesParams = new VmTemplateParametersBase(vmtGuid);
        ArrayList<Guid> storages = new ArrayList<Guid>();
        storages.add(basicSetup.getStorage().getid());
        removeImagesParams.setStorageDomainsList(storages);
        Backend.getInstance().runInternalAction(
                VdcActionType.RemoveAllVmTemplateImageTemplates, removeImagesParams);
        backend.RunAction(VdcActionType.RemoveVmTemplate, removeImagesParams);
        System.out.println("-- remove template and its images -- ");
    }

}
