package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * Base class for comparing between vdss
 */
public abstract class VdsComparer {
    /**
     * Factory method, creates necessary comparer
     *
     * @return
     */
    public static VdsComparer CreateComparer(VdsSelectionAlgorithm selectionAlgorithm) {
        switch (selectionAlgorithm) {
        case EvenlyDistribute:
            return new EvenlyDistributeComparer();
        case PowerSave:
            return new PowerSaveComparer();
        case None:
            return new NoneComparer();
        default:
            throw new NotImplementedException("Uknown type of selection algorithm: " + selectionAlgorithm);
        }
        // try
        // {
        // //return AppDomain.CurrentDomain.CreateInstanceAndUnwrap("VdcBLL",
        // GetComparerTypeName(selectionAlgorithm)) as VdsComparer;
        // java.lang.Class type =
        // java.lang.Class.forName(GetComparerTypeName(selectionAlgorithm));
        // //type.GetConstructor(BindingFlags.Instance | BindingFlags.NonPublic,
        // null,
        // // CallingConventions.HasThis, null, null);
        // java.lang.reflect.Constructor info = type.getConstructors()[0];
        // Object TempAsCast = info.newInstance(null);
        // return (VdsComparer)((TempAsCast instanceof VdsComparer) ? TempAsCast
        // : null);
        // //return new BestDistributionComparer();
        // }
        // catch (Exception ex)
        // {
        // throw new ApplicationException("JTODO unhandled exception", ex);
        // }
    }

    // private static String GetComparerTypeName(VdsSelectionAlgorithm
    // selectionAlgorithm)
    // {
    // return String.format("%1$s.%2$s%3$s",
    // "VdcBLL",selectionAlgorithm.toString(), "Comparer");
    // }
    /**
     * Base abstract function for finish best Vds treatment
     *
     * @param x
     */
    public abstract void BestVdsProcedure(VDS x);

    /**
     * Base abstract function to compare between two VDSs
     *
     * @param x
     * @param y
     * @param vm
     * @return
     */
    public abstract boolean IsBetter(VDS x, VDS y, VM vm);
}
