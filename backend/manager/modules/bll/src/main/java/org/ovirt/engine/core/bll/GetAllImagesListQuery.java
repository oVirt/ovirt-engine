package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;

public class GetAllImagesListQuery<P extends GetAllIsoImagesListParameters> extends AbstractGetAllImagesListByStorageDomainIdQuery<P> {
    public GetAllImagesListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected FileTypeExtension getFileTypeExtension() {
        return FileTypeExtension.All;
    }

    private List<RepoFileMetaData> getFileListForExtension(FileTypeExtension extension) {
         return IsoDomainListSyncronizer.getInstance().getUserRequestForStorageDomainRepoFileList
                (getStorageDomainId(),
                        extension,
                        getParameters().getForceRefresh());
    }

    @Override
    protected List<RepoFileMetaData> getUserRequestForStorageDomainRepoFileList() {
        List<RepoFileMetaData> fileList = new ArrayList<RepoFileMetaData>();
        fileList.addAll(getFileListForExtension(FileTypeExtension.ISO));
        fileList.addAll(getFileListForExtension(FileTypeExtension.Floppy));
        return fileList;
    }
}
