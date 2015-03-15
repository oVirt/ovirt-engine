package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

import java.util.List;

@SuppressWarnings("unused")
public class GetImageByIdQuery<P extends GetImageByIdParameters> extends QueriesCommandBase<P> {

    public GetImageByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<RepoImage> imageList = IsoDomainListSyncronizer.getInstance()
                .getUserRequestForStorageDomainRepoFileList(
                        getParameters().getStorageDomainId(), ImageFileType.All, true);

        getQueryReturnValue().setReturnValue(LinqUtils.firstOrNull(imageList, new Predicate<RepoImage>() {
            @Override
            public boolean eval(RepoImage repoImage) {
                return repoImage.getRepoImageId().equals(getParameters().getRepoImageId());
            }
        }));
    }

}
