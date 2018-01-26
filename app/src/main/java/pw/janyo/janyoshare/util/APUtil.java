package pw.janyo.janyoshare.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

public class APUtil {
    /**
     * 私有构造函数保证调用静态方法
     */
    private APUtil() {
    }

    public static boolean isAPOn(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return false;
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static boolean openAP(Context context, String SSID, String password) {
        if (TextUtils.isEmpty(SSID))
            return false;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return false;
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);
        WifiConfiguration wifiConfiguration = getAPConfig(SSID, password);
        try {

        }
    }

    private static WifiConfiguration getAPConfig(String SSID, String password) {
        if (TextUtils.isEmpty(SSID))
            return null;
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = SSID;
        configuration.preSharedKey = password;
//        configuration.hiddenSSID=true;
        configuration.status = WifiConfiguration.Status.ENABLED;
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return configuration;
    }
}
