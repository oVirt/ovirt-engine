package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.core.compat.Guid;

public class IconTestHelpler {

    public static final String MEDIA_TYPE = "image/png";
    public static final String DATA_URL = "iVBORw0KGgoAAAANSUhEUgAAAJYAAAB4CAYAAAAQTwsQAAADNklEQVR4nO3cTUpbYQCF4aPioBZ0AdoN9G8FFgSdNNQOFe0KbEe2VCcKWY9TR8VRodz7xWZP6SBKLdWg1yTnevI+cCeOzHdeot7ESAAAAAAAAAAAwKBIg9TLfbYzzT0+YYVyj09YodzjE1Yo9/iEFco9PmGFco9PWKHc4xNWKPf4hBXKPT5hhXKPT1ih3OMTVij3+IQVyj0+YYVyj09YodzjE1Yo9/iEFco9PmGFco9PWKHc4xNWKPf4hBXKPT5hhXKPT1ih3OMTVij3+IQVyj0+YYVyj09YodzjE1Yo9/iEFco9PmGFco9PWKHc4xNWKPf4hBXKPT5hhXKPT1ih3OMTVij3+IQVyj0+YYVyj09YodzjE1Yo9/iEFco9PmGFco9PWKHc4xNWKPf4hBXKPT5hhXKPT1ih3OMTVij3+IQVyj0+YSFPV/MqWlNP71RrV0VfVOtYRaeqteH+9vDUXGhFtbZU9E21uiOuDfe3iqegryXV2r56RhoVFHHhniq9Ua3jBwRFXBjhTAuq9bFhUMSFW/S1qFqfxhAVceHK8JlqnFERF6Qx/fgjLtxQ9HaCURHXTPqh5+rpaAphEddM6enDlKIirplxoZUH3vwkLtxDrS1DVMQVbaA51fpqDIu4Iv3UC3NUxBVp+NYXd1TEFafWbguCIq44RZ9bEBNxxWn+lhjiwgie+1fEFa/NYRWduo8HTbX3R2FXPR25jwdNtfOX9+vrwH08aKp9txtuXjvu40FT7bpB+u9VtO4+HjRVtGYP6K6r0qr7eNBUO16E/v/q6VADzbmPB4/hfdvM7delNt3HgscqWm7Z/awTFS27jwXjUE39rcl3X0Ud93FgXM61NMV/prj76um7Kj1zHwfGafg5Dd6wfumV+xgwCbW2jWG9dz98TMqZFlRpb+pRVdpTV/Puh49J6mtRlfanGNW++lp0P2xMw5kWpvJPrEUdDXimmj2/9Vr1RP5aPFLRS/fDg9O5llTUGdNN1BMVdbilgL+KljV8+eewQVCHutQmd9Rxt4HmVGlVReuqtaNaB7r+OO7hs9rx1dd2VLSuSqu8oAwAAAAAAAAAAAAAAAAAAAAAQDv9AeKA+xpCWfFnAAAAAElFTkSuQmCC";

    private IconTestHelpler() {}

    public static Icon createIcon(Guid id) {
        final Icon icon = new Icon();
        icon.setId(id.toString());
        return icon;
    }

    public static Icon createIconWithData() {
        final Icon icon = new Icon();
        icon.setMediaType(MEDIA_TYPE);
        icon.setData(DATA_URL);
        return icon;
    }
}
