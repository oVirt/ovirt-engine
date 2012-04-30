package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;

public class AddVmTemplateFromImportParameters extends AddVmTemplateParameters {
    private static final long serialVersionUID = 3675626310536138446L;

    private CandidateInfoParameters _candidateInfoParameters;

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    protected void setForce(boolean value) {
        privateForce = value;
    }

    private String _vmTemplateNewName;

    /**
     * Use this c'tor when you want to use the candidate's name as the name of the imported template.
     *
     * @param candidateInfoParameters
     *            the candidate info parameters
     * @param force
     *            force override if template with the same GUID already exists in the VDC
     */

    public AddVmTemplateFromImportParameters() {
        _vmTemplateNewName = null;
    }

    public AddVmTemplateFromImportParameters(CandidateInfoParameters candidateInfoParameters, boolean force) {
        super(new VmStatic(), "", "");
        _vmTemplateNewName = null;
        _candidateInfoParameters = candidateInfoParameters;
        setForce(force);
    }

    /**
     * Use this c'tor when you want to give a new name to the imported template other than the candidate's name.
     *
     * @param candidateInfoParameters
     *            the candidate info parameters
     * @param vmNewName
     *            the name that the imported template will be given
     * @param force
     *            force override if template with the same GUID already exists in the VDC
     */
    public AddVmTemplateFromImportParameters(CandidateInfoParameters candidateInfoParameters, String vmTemplateNewName,
            boolean force) {
        super(new VmStatic(), "", "");
        _candidateInfoParameters = candidateInfoParameters;
        _vmTemplateNewName = vmTemplateNewName;
        setForce(force);
    }

    public CandidateInfoParameters getCandidateInfoParams() {
        return _candidateInfoParameters;
    }

    public String getVmTemplateNewName() {
        return _vmTemplateNewName;
    }

}
