package pw.janyo.janyoshare.util.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import pw.janyo.janyoshare.util.Constant;
import vip.mystery0.tools.logs.Logs;

public class WIFIUtil {
    private static final String TAG = "WIFIUtil";
    public static final int WIFI_CIPHER_NO_PASS = 0;
    public static final int WIFI_CIPHER_WEP = 1;
    public static final int WIFI_CIPHER_WPA = 2;

    private Context context;
    private WifiManager wifiManager;


    public WIFIUtil(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }


    /**
     * 打开Wi-Fi
     */
    public void openWifi() {
        Logs.i(TAG, "openWifi: ");
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wi-Fi
     */
    public void closeWifi() {
        Logs.i(TAG, "closeWifi: ");
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 当前WiFi是否开启
     */
    public boolean isWifiEnabled() {
        Logs.i(TAG, "isWifiEnabled: " + wifiManager.isWifiEnabled());
        return wifiManager.isWifiEnabled();
    }

    /**
     * 清除指定网络
     *
     * @param SSID 网络名称
     */
    public void clearWifiInfo(String SSID) {
        WifiConfiguration tempConfig = isExists(SSID);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
    }

    /**
     * 判断当前网络是否WiFi
     */
    public boolean isWifi() {
        Logs.i(TAG, "isWifi: ");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == 1;
    }

    /**
     * 获取周围可用WiFi扫描结果
     */
    public List<ScanResult> scanWIFI() {
        Logs.i(TAG, "scanWIFI: ");
        if (!isWifiEnabled() || !wifiManager.startScan())
            return new ArrayList<>();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null && scanResults.size() > 0) {
            return filterScanResult(scanResults);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取当前连接WiFi的SSID
     */
    public String getConnectedSSID() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null ? wifiInfo.getSSID().replaceAll("\"", "") : "";
    }

    /**
     * 连接WiFi
     */
    public boolean connectWiFi(String SSID) {
        int netID = wifiManager.addNetwork(createWiFiInfo(SSID, null, WIFI_CIPHER_NO_PASS));
        boolean enable = wifiManager.enableNetwork(netID, true);
        return enable && wifiManager.reconnect();
    }

    /**
     * 断开指定ID的网络
     */
    public boolean disconnectWifi(String SSID) {
        Logs.i(TAG, "disconnectWifi: ");
        return wifiManager.disableNetwork(getNetworkIdBySSID(SSID)) && wifiManager.disconnect();
    }

    /**
     * 清除指定SSID的网络
     */
    public void clearWifiConfig(String SSID) {
        SSID = SSID.replace("\"", "");
        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        if (wifiConfigurations != null && wifiConfigurations.size() > 0) {
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if (wifiConfiguration.SSID.replace("\"", "").contains(SSID)) {
                    wifiManager.removeNetwork(wifiConfiguration.networkId);
                    wifiManager.saveConfiguration();
                }
            }
        }
    }

    /**
     * 清除当前连接的WiFi网络
     */
    public void clearWifiConfig() {
        String SSID = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        if (wifiConfigurations != null && wifiConfigurations.size() > 0) {
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if (wifiConfiguration.SSID.replace("\"", "").contains(SSID)) {
                    wifiManager.removeNetwork(wifiConfiguration.networkId);
                    wifiManager.saveConfiguration();
                }
            }
        }
    }

    /**
     * 根据SSID查networkID
     */
    public int getNetworkIdBySSID(String SSID) {
        if (TextUtils.isEmpty(SSID)) {
            return 0;
        }
        WifiConfiguration config = isExists(SSID);
        if (config != null) {
            return config.networkId;
        }
        return 0;
    }

    /**
     * 获取连接WiFi后的IP地址
     */
    public String getIPAddressFromHotspot() throws UnknownHostException {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int hostAddress = wifiInfo.getIpAddress();
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
        return InetAddress.getByAddress(addressBytes).toString();
    }

    /**
     * 创建WifiConfiguration对象 分为三种情况：1没有密码;2用wep加密;3用wpa加密
     */
    public WifiConfiguration createWiFiInfo(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExists(SSID);
        if (tempConfig != null)
            wifiManager.removeNetwork(tempConfig.networkId);

        switch (type) {
            case WIFI_CIPHER_NO_PASS:
//                config.wepKeys[0] = "";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                config.wepTxKeyIndex = 0;
                break;
            case WIFI_CIPHER_WEP:
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
                break;
            case WIFI_CIPHER_WPA:
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return config;
    }

    /**
     * 获取当前手机所连接的wifi信息
     */
    public WifiInfo getCurrentWifiInfo() {
        return wifiManager.getConnectionInfo();
    }

    /**
     * 获取指定WiFi信息
     */
    private WifiConfiguration isExists(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null && existingConfigs.size() > 0) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(SSID) || existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    /**
     * 根据SSID过滤WiFi扫描结果
     */
    private List<ScanResult> filterScanResult(List<ScanResult> scanResults) {
        List<ScanResult> result = new ArrayList<>();
        if (scanResults == null) {
            return result;
        }

        for (ScanResult scanResult : scanResults) {
            if (!TextUtils.isEmpty(scanResult.SSID) && scanResult.SSID.startsWith(Constant.WIFI_SSID)) {
                result.add(scanResult);
            }
        }
        return sortScanResultWithLevel(result);
    }

    /**
     * 将搜索到的wifi根据信号强度从强到弱进行排序
     *
     * @param scanResultList 源列表
     * @return 排序后列表
     */
    private List<ScanResult> sortScanResultWithLevel(List<ScanResult> scanResultList) {
        for (int i = 0; i < scanResultList.size(); i++) {
            for (int j = 0; j < scanResultList.size(); j++) {
                if (scanResultList.get(i).level > scanResultList.get(j).level) {
                    ScanResult temp = scanResultList.get(i);
                    scanResultList.set(i, scanResultList.get(j));
                    scanResultList.set(j, temp);
                }
            }
        }
        return scanResultList;
    }
}
