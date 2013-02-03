package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class ImagesListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String IMAGES_LIST = "imageslist";
    private String[] mImageList;

    public ImagesListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(IMAGES_LIST);
        if (tempObj != null) {
            mImageList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                mImageList[i] = (String) tempObj[i];
            }
        }
    }

    public String[] getImageList() {
        return mImageList;
    }
}
