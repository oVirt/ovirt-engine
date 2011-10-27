package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVMFromImportCandidateParameters")
public class AddVMFromImportCandidateParameters extends AddVmFromScratchParameters {
    private static final long serialVersionUID = -3570212254700154211L;

    @XmlElement
    private CandidateInfoParameters _candidateInfoParameters;

    @XmlElement(name = "Force")
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    protected void setForce(boolean value) {
        privateForce = value;
    }

    @XmlElement
    private String _vmNewName;

    /**
     * Use this c'tor when you want to use the candidate's name as the name of the imported VM.
     *
     * @param candidateInfoParameters
     *            the candidate info parameters
     * @param force
     *            force override if VM with the same GUID already exists in the VDC (What about VmWare?...)
     */

    public AddVMFromImportCandidateParameters() {
        _vmNewName = null;
    }

    public AddVMFromImportCandidateParameters(CandidateInfoParameters candidateInfoParameters, boolean force,
            Guid storageDomainId) {
        super(new VmStatic(), new java.util.ArrayList<DiskImageBase>(
                java.util.Arrays.asList(new DiskImageBase[] { new DiskImageBase() })), storageDomainId);
        _vmNewName = null;
        _candidateInfoParameters = candidateInfoParameters;
        setForce(force);
    }

    /**
     * Use this c'tor when you want to give a new name to the imported VM other than the candidate's name.
     *
     * @param candidateInfoParameters
     *            the candidate info parameters
     * @param vmNewName
     *            the name that the imported VM will be given
     * @param force
     *            force override if VM with the same GUID already exists in the VDC (What about VmWare?...)
     */
    public AddVMFromImportCandidateParameters(CandidateInfoParameters candidateInfoParameters, String vmNewName,
            boolean force, Guid storageDomainId) {
        super(new VmStatic(), new java.util.ArrayList<DiskImageBase>(
                java.util.Arrays.asList(new DiskImageBase[] { new DiskImageBase() })), storageDomainId);
        _candidateInfoParameters = candidateInfoParameters;
        _vmNewName = vmNewName;
        setForce(force);
    }

    public CandidateInfoParameters getCandidateInfoParams() {
        return _candidateInfoParameters;
    }

    public String getVmNewName() {
        return _vmNewName;
    }

}
