package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetImageInfoVDSCommand<P extends GetImageInfoVDSCommandParameters> extends IrsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(GetImageInfoVDSCommand.class);

    protected OneImageInfoReturnForXmlRpc imageInfoReturn;

    @Override
    protected Object getReturnValueFromBroker() {
        return imageInfoReturn;
    }

    public GetImageInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        imageInfoReturn = getIrsProxy().getVolumeInfo(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());
        DiskImage di = null;
        try {
            proceedProxyReturnValue();
            di = buildImageEntity(imageInfoReturn.getInfo());
        } catch (Exception e) {
            printReturnValue();
            // nothing to do - logging inside upper functions
        } finally {
            // if couldn't parse image then succeeded should be false
            getVDSReturnValue().setSucceeded(di != null);
            setReturnValue(di);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return imageInfoReturn.getXmlRpcStatus();
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        if (returnStatus != EngineError.Done) {
            log.error(
                    "IrsBroker::getImageInfo::Failed getting image info imageId='{}' does not exist on domainName='{}', domainId='{}', error code: '{}', message: {}",
                    getParameters().getImageId(),
                    DbFacade.getInstance().getStorageDomainStaticDao()
                            .get(getParameters().getStorageDomainId())
                            .getStorageName(),
                    getParameters().getStorageDomainId(),
                    returnStatus,
                    imageInfoReturn.getXmlRpcStatus().message);
            throw new IRSErrorException(returnStatus.toString());
        }
    }

    public DiskImage buildImageEntity(Map<String, Object> xmlRpcStruct) {
        DiskImage newImage = new DiskImage();
        try {
            newImage.setImageId(new Guid((String) xmlRpcStruct.get(IrsProperties.uuid)));

            newImage.setParentId(new Guid((String) xmlRpcStruct.get(IrsProperties.parent)));
            newImage.setDescription((String) xmlRpcStruct.get(IrsProperties.description));
            newImage.setImageStatus(EnumUtils.valueOf(ImageStatus.class,
                    (String) xmlRpcStruct.get(IrsProperties.ImageStatus), true));
            if (xmlRpcStruct.containsKey(IrsProperties.size)) {
                newImage.setSize(Long.parseLong(xmlRpcStruct.get(IrsProperties.size).toString()) * 512);
            }
            if (xmlRpcStruct.containsKey("capacity")) {
                newImage.setSize(Long.parseLong(xmlRpcStruct.get("capacity").toString()));
            }
            if (xmlRpcStruct.containsKey("truesize")) {
                newImage.setActualSizeInBytes(Long.parseLong(xmlRpcStruct.get("truesize").toString()));
            }
            if (xmlRpcStruct.containsKey("ctime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.get("ctime").toString());
                newImage.setCreationDate(makeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.containsKey("mtime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.get("mtime").toString());
                newImage.setLastModifiedDate(makeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.containsKey("domain")) {
                newImage.setStorageIds(new ArrayList<>(Arrays.asList(new Guid(xmlRpcStruct.get("domain").toString()))));
            }
            if (xmlRpcStruct.containsKey("image")) {
                newImage.setId(new Guid(xmlRpcStruct.get("image").toString()));
            }
            if (xmlRpcStruct.containsKey("type")) {
                newImage.setVolumeType(EnumUtils.valueOf(VolumeType.class, xmlRpcStruct.get("type").toString(),
                        true));
            }
            if (xmlRpcStruct.containsKey("format")) {
                newImage.setVolumeFormat(EnumUtils.valueOf(VolumeFormat.class, xmlRpcStruct.get("format")
                        .toString(), true));
            }
        } catch (RuntimeException ex) {
            log.error("irsBroker::buildImageEntity::Failed building DIskImage: {}", ex.getMessage());
            printReturnValue();
            log.debug("Exception", ex);
            newImage = null;
        }

        return newImage;
    }

    private static Date makeDTFromCTime(long ctime) {
        return new Date(ctime * 1000L);
    }
}
