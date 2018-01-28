package pw.janyo.janyoshare.util.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

import vip.mystery0.tools.logs.Logs;

/**
 * Created by AA on 2017/3/22.
 * Modified by Mystery0 on 2018/1/26
 */
public class APUtil {
    private static final String TAG = "APUtil";
    private Context context;
    private WifiManager wifiManager;

    public APUtil(Context context){
        this.context=context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 便携热点是否开启
     *
     * @return 热点是否开启
     */
    public boolean isAPOn() {
        Logs.i(TAG, "isAPOn: ");
        try {
            @SuppressLint("PrivateApi")
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开启便携热点
     *
     * @param SSID    便携热点SSID
     * @return 结果
     */
    public boolean openAP(String SSID) {
        Logs.i("TAG", "openAP: ");
        if (TextUtils.isEmpty(SSID)) {
            return false;
        }

        WIFIUtil wifiUtil=new WIFIUtil(context);
        wifiUtil.closeWifi();

        WifiConfiguration wifiConfiguration = getAPConfig(SSID);
        try {
            if (isAPOn()) {
                wifiManager.setWifiEnabled(false);
                closeAP();
            }

            //使用反射开启Wi-Fi热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, wifiConfiguration, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭便携热点
     */
    public void closeAP() {
        Logs.i(TAG, "closeAP: ");
        try {
            //等待适配8.0
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取开启便携热点后自身热点IP地址
     */
    public String getHotspotLocalIpAddress() {
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            int address = dhcpInfo.serverAddress;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }

    /**
     * 设置没有密码的热点信息
     *
     * @param SSID 便携热点SSID
     */
    private WifiConfiguration getAPConfig(String SSID) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = SSID;
//        config.hiddenSSID = true;
        config.status = WifiConfiguration.Status.ENABLED;
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return config;
    }
}