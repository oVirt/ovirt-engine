package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpNative;

/**
 * Class for generating an RDP descriptor and invoking native RDP client.
 * This method of console invocation doesn't support automatic login.
 *
 */
public class RdpNativeImpl extends AbstractRdp implements IRdpNative {

    private static final String BASE_CONFIG_FILE =
        "session bpp:i:32\n" + //$NON-NLS-1$
        "winposstr:s:0,3,0,0,800,600\n" + //$NON-NLS-1$
        "compression:i:1\n" + //$NON-NLS-1$
        "keyboardhook:i:2\n" + //$NON-NLS-1$
        "audiocapturemode:i:0\n" + //$NON-NLS-1$
        "videoplaybackmode:i:1\n" + //$NON-NLS-1$
        "connection type:i:2\n" + //$NON-NLS-1$
        "displayconnectionbar:i:1\n" + //$NON-NLS-1$
        "disable wallpaper:i:1\n" + //$NON-NLS-1$
        "allow font smoothing:i:0\n" + //$NON-NLS-1$
        "allow desktop composition:i:0\n" + //$NON-NLS-1$
        "disable full window drag:i:1\n" + //$NON-NLS-1$
        "disable menu anims:i:1\n" + //$NON-NLS-1$
        "disable themes:i:0\n" + //$NON-NLS-1$
        "disable cursor setting:i:0\n" + //$NON-NLS-1$
        "bitmapcachepersistenable:i:1\n" + //$NON-NLS-1$
        "audiomode:i:0\n" + //$NON-NLS-1$
        "redirectcomports:i:0\n" + //$NON-NLS-1$
        "redirectposdevices:i:0\n" + //$NON-NLS-1$
        "redirectdirectx:i:1\n" + //$NON-NLS-1$
        "autoreconnection enabled:i:1\n" + //$NON-NLS-1$
        "prompt for credentials:i:1\n" + //$NON-NLS-1$
        "negotiate security layer:i:1\n" + //$NON-NLS-1$
        "remoteapplicationmode:i:0\n" + //$NON-NLS-1$
        "alternate shell:s:\n" + //$NON-NLS-1$
        "shell working directory:s:\n" + //$NON-NLS-1$
        "gatewayhostname:s:\n" + //$NON-NLS-1$
        "gatewayusagemethod:i:4\n" + //$NON-NLS-1$
        "gatewaycredentialssource:i:4\n" + //$NON-NLS-1$
        "gatewayprofileusagemethod:i:0\n" + //$NON-NLS-1$
        "promptcredentialonce:i:1\n" + //$NON-NLS-1$
        "use redirection server name:i:0";//$NON-NLS-1$

    private int getScreenMode() {
        if (getFullScreen()) {
            return 2;
        }

        return 1;
    }

    //falsy value to 0; 1 otherwise
    private int booleanToInt(Boolean b) {
        if (b == null || Boolean.FALSE.equals(b)) {
            return 0;
        }

        return 1;
    }

    private String getredirectDrivesLines() {
        if (getUseLocalDrives()) {
            return "\ndrivestoredirect:s:*"; //$NON-NLS-1$
        } else {
            return "\ndrivestoredirect:s:";  //$NON-NLS-1$
        }
    }

    @Override
    public void connect() {
        StringBuilder configBuilder = new StringBuilder(BASE_CONFIG_FILE); //$NON-NLS-1$
        configBuilder.append("\nscreen mode id:i:").append(getScreenMode()); //$NON-NLS-1$
        configBuilder.append("\ndesktopwidth:i:").append(getWidth()); //$NON-NLS-1$
        configBuilder.append("\ndesktopheight:i:").append(getHeight()); //$NON-NLS-1$
        configBuilder.append("\nauthentication level:i:").append(getAuthenticationLevel()); //$NON-NLS-1$
        configBuilder.append("\nfull address:s:").append(getAddress()); //$NON-NLS-1$
        configBuilder.append("\nenablecredsspsupport:i:").append(booleanToInt(getEnableCredSspSupport())); //$NON-NLS-1$
        configBuilder.append(getredirectDrivesLines());
        configBuilder.append("\nredirectprinters:i:").append(booleanToInt(getRedirectPrinters()));//$NON-NLS-1$
        configBuilder.append("\nredirectsmartcards:i:").append(booleanToInt(getRedirectSmartCards()));//$NON-NLS-1$
        configBuilder.append("\nredirectclipboard:i:").append(booleanToInt(getRedirectClipboard()));//$NON-NLS-1$
        configBuilder.append("\nusername:s:").append(getUserNameAndDomain());//$NON-NLS-1$

        ConsoleModel.makeConsoleConfigRequest("console.rdp", "application/rdp; charset=UTF-8", configBuilder.toString());//$NON-NLS-1$$NON-NLS-2$
    }

    @Override
    public boolean getEnableCredSspSupport() {
        return true;
    }

}
