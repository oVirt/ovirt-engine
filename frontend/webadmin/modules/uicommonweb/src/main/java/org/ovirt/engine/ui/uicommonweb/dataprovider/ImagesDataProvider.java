package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Converter;
import org.ovirt.engine.ui.frontend.Frontend;

public class ImagesDataProvider {

    public static final String ISO_PREFIX = "iso://"; //$NON-NLS-1$

    public static void getFloppyImageList(AsyncQuery<List<String>> aQuery, Guid storagePoolId) {
        getIrsImageList(aQuery, storagePoolId, false, ImageFileType.Floppy);
    }

    public static void getUnknownImageList(AsyncQuery<List<String>> aQuery, Guid storagePoolId, boolean forceRefresh) {
        getIrsImageList(aQuery,
                storagePoolId,
                forceRefresh,
                ImageFileType.All,
                new RepoImageToImageFileNameAsyncConverter() {

                    @Override
                    protected String transform(ArrayList<String> fileNameList, RepoImage repoImage) {
                        return ISO_PREFIX + super.transform(fileNameList, repoImage);
                    }

                    @Override
                    protected boolean desiredImage(RepoImage repoImage) {
                        return ImageFileType.Unknown == repoImage.getFileType();
                    }
                });
    }

    public static void getIrsImageList(AsyncQuery<List<String>> aQuery, Guid storagePoolId) {
        getIrsImageList(aQuery, storagePoolId, false);
    }

    public static void getIrsImageList(AsyncQuery<List<String>> aQuery, Guid storagePoolId, boolean forceRefresh) {
        ImageFileType imageFileType = ImageFileType.ISO;
        getIrsImageList(aQuery, storagePoolId, forceRefresh, imageFileType);
    }

    public static void getIrsImageList(AsyncQuery<List<String>> aQuery,
            Guid storagePoolId,
            boolean forceRefresh,
            ImageFileType imageFileType) {

        getIrsImageList(aQuery,
                storagePoolId,
                forceRefresh,
                imageFileType,
                new RepoImageToImageFileNameAsyncConverter());
    }

    private static void getIrsImageList(AsyncQuery<List<String>> aQuery,
            Guid storagePoolId,
            boolean forceRefresh,
            ImageFileType imageFileType,
            Converter<List<String>, List<RepoImage>> converterCallBack) {

        aQuery.converterCallback = converterCallBack;

        GetImagesListByStoragePoolIdParameters parameters =
                new GetImagesListByStoragePoolIdParameters(storagePoolId, imageFileType);
        parameters.setForceRefresh(forceRefresh);
        Frontend.getInstance().runQuery(QueryType.GetImagesListByStoragePoolId, parameters, aQuery);
    }


    private static class RepoImageToImageFileNameAsyncConverter implements Converter<List<String>, List<RepoImage>> {
        @Override
        public List<String> convert(List<RepoImage> source) {
            if (source != null) {
                ArrayList<String> fileNameList = new ArrayList<>();
                for (RepoImage repoImage : source) {
                    if (desiredImage(repoImage)) {
                        fileNameList.add(transform(fileNameList, repoImage));
                    }
                }

                Collections.sort(fileNameList, new LexoNumericComparator());
                return fileNameList;
            }
            return new ArrayList<>();
        }

        protected String transform(ArrayList<String> fileNameList, RepoImage repoImage) {
            return repoImage.getRepoImageId();
        }

        protected boolean desiredImage(RepoImage repoImage) {
            return true;
        }
    }
}
