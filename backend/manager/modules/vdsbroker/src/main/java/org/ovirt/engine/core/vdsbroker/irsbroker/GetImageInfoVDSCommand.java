package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetImageInfoVDSCommand<P extends GetImageInfoVDSCommandParameters> extends IrsBrokerCommand<P> {
    protected OneImageInfoReturnForXmlRpc imageInfoReturn;

    @Override
    protected Object getReturnValueFromBroker() {
        return imageInfoReturn;
    }

    public GetImageInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        imageInfoReturn = getIrsProxy().getVolumeInfo(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());
        DiskImage di = null;
        try {
            ProceedProxyReturnValue();
            di = buildImageEntity(imageInfoReturn.mInfo);
        } catch (java.lang.Exception e) {
            PrintReturnValue();
            // nothing to do - logging inside upper functions
        } finally {
            // if couldn't parse image then succeeded should be false
            getVDSReturnValue().setSucceeded((di != null));
            setReturnValue(di);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return imageInfoReturn.mStatus;
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        if (returnStatus != VdcBllErrors.Done) {
            log.errorFormat(
                    "IrsBroker::getImageInfo::Failed getting image info imageId = {0} does not exist on domainName = {1} , domainId = {2},  error code: {3}, message: {4}",
                    getParameters().getImageId().toString(),
                    DbFacade.getInstance().getStorageDomainStaticDao()
                            .get(getParameters().getStorageDomainId())
                            .getStorageName(),
                    getParameters()
                            .getStorageDomainId().toString(),
                    returnStatus
                            .toString(),
                    imageInfoReturn.mStatus.mMessage);
            throw new IRSErrorException(returnStatus.toString());
        }
    }

    /**
     * <exception>VdcDAL.IrsBrokerIRSErrorException.
     */
    public DiskImage buildImageEntity(XmlRpcStruct xmlRpcStruct) {
        DiskImage newImage = new DiskImage();
        try {
            newImage.setImageId(new Guid((String) xmlRpcStruct.getItem(IrsProperties.uuid)));

            newImage.setParentId(new Guid((String) xmlRpcStruct.getItem(IrsProperties.parent)));
            newImage.setDescription((String) xmlRpcStruct.getItem(IrsProperties.description));
            newImage.setImageStatus(EnumUtils.valueOf(ImageStatus.class,
                    (String) xmlRpcStruct.getItem(IrsProperties.ImageStatus), true));
            if (xmlRpcStruct.contains(IrsProperties.size)) {
                newImage.setSize(Long.parseLong(xmlRpcStruct.getItem(IrsProperties.size).toString()) * 512);
            }
            if (xmlRpcStruct.contains("apparentsize")) {
                newImage.setActualSizeFromDiskImage(Long.parseLong(xmlRpcStruct.getItem("apparentsize").toString()) * 512);
            }
            if (xmlRpcStruct.contains("capacity")) {
                newImage.setSize(Long.parseLong(xmlRpcStruct.getItem("capacity").toString()));
            }
            if (xmlRpcStruct.contains("truesize")) {
                newImage.setActualSizeFromDiskImage(Long.parseLong(xmlRpcStruct.getItem("truesize").toString()));
            }
            if (xmlRpcStruct.contains("ctime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.getItem("ctime").toString());
                newImage.setCreationDate(MakeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.contains("mtime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.getItem("mtime").toString());
                newImage.setLastModifiedDate(MakeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.contains("domain")) {
                newImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(new Guid(xmlRpcStruct.getItem("domain").toString()))));
            }
            if (xmlRpcStruct.contains("image")) {
                newImage.setimage_group_id(new Guid(xmlRpcStruct.getItem("image").toString()));
            }
            if (xmlRpcStruct.contains("type")) {
                newImage.setVolumeType(EnumUtils.valueOf(VolumeType.class, xmlRpcStruct.getItem("type").toString(),
                        true));
            }
            if (xmlRpcStruct.contains("format")) {
                newImage.setvolumeFormat(EnumUtils.valueOf(VolumeFormat.class, xmlRpcStruct.getItem("format")
                        .toString(), true));
            }
        } catch (RuntimeException ex) {
            log.errorFormat("irsBroker::buildImageEntity::Failed building DIskImage");
            PrintReturnValue();
            log.error(ex.getMessage(), ex);
            newImage = null;
        }

        return newImage;
    }

    private static java.util.Date MakeDTFromCTime(long ctime) {
        return new Date(ctime * 1000L);
    }

    private static Log log = LogFactory.getLog(GetImageInfoVDSCommand.class);
}
