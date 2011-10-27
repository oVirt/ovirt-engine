package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public interface IIrsBroker {

    /**
     * create a new Image
     *
     * @param imageName
     *            string Name
     * @param imageSizeInBytes
     *            int number of bytes
     * @param imageType
     *            int, for future use
     * @return Guid <exception>VdcDAL.IrsBroker.IRSProtocolException <exception>VdcDAL.IrsBroker.IRSNetworkException
     *         <exception>VdcDAL.IrsBrokerIRSErrorException. <exception>System.Exception
     */
    Guid create(String imageName, long imageSizeInBytes, int imageType);

    /**
     * Lists all images on IRS
     *
     * @return Guid[] <exception>VdcDAL.IrsBroker.IRSProtocolException <exception>VdcDAL.IrsBroker.IRSNetworkException
     *         <exception>VdcDAL.IrsBrokerIRSErrorException. <exception>System.Exception
     */
    Guid[] listImageIds();

    /**
     * Destroy an Image
     *
     * @param imageId
     *            Guid ImageId <exception>VdcDAL.IrsBroker.IRSProtocolException
     *            <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *            <exception>System.Exception
     */
    void destroy(Guid imageId);

    /**
     * Gets Information about the Image
     *
     * @param imageId
     *            Guid
     * @return DislImage <exception>VdcDAL.IrsBroker.IRSProtocolException
     *         <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *         <exception>System.Exception
     */
    DiskImage getImageInfo(Guid imageId);

    /**
     * Copy an Image
     *
     * @param srcImageId
     * @return Guid - imageId of the copied image <exception>VdcDAL.IrsBroker.IRSProtocolException
     *         <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *         <exception>System.Exception
     */
    Guid copyImage(Guid srcImageId);

    /**
     * Create a snapshot of an image
     *
     * @param srcImageId
     *            Guid
     * @return Guid - the id of the new created snapshot <exception>VdcDAL.IrsBroker.IRSProtocolException
     *         <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *         <exception>System.Exception
     */
    Guid createSnapshot(Guid srcImageId);

    /**
     * Gets the parent image
     *
     * @param imageId
     *            Guid - the child image
     * @return Guid - the image id of the parent image <exception>VdcDAL.IrsBroker.IRSProtocolException
     *         <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *         <exception>System.Exception
     */
    Guid getParent(Guid imageId);

    /**
     * Merge 2 snapshots into one
     *
     * @param imageId1
     *            Guid - id of first mage
     * @param imageId2
     *            Guid - id of second image <exception>VdcDAL.IrsBroker.IRSProtocolException
     *            <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *            <exception>System.Exception
     */
    void mergeSnapshots(Guid imageId1, Guid imageId2);

    /**
     * Sets the image description
     *
     * @param imageId
     *            Guid Image id
     * @param descr
     *            string description <exception>VdcDAL.IrsBroker.IRSProtocolException
     *            <exception>VdcDAL.IrsBroker.IRSNetworkException <exception>VdcDAL.IrsBrokerIRSErrorException.
     *            <exception>System.Exception
     */
    void setImageDescr(Guid imageId, String descr);

}
