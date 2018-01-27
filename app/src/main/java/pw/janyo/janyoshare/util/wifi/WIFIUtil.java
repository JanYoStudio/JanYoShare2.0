package pw.janyo.janyoshare.util.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WIFIUtil {
    private static final String TAG = "WIFIUtil";
    //过滤免密码连接的WiFi
    public static final String NO_PASSWORD = "[ESS]";
    public static final String NO_PASSWORD_WPS = "[WPS][ESS]";

    private Context mContext;
    private WifiManager mWifiManager;


    public WIFIUtil(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }


    /**
     * 打开Wi-Fi
     */
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wi-Fi
     */
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 当前WiFi是否开启
     */
    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 清除指定网络
     *
     * @param SSID 网络名称
     */
    public void clearWifiInfo(String SSID) {
        WifiConfiguration tempConfig = isExists(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
    }

    /**
     * 判断当前网络是否WiFi
     */
    public boolean isWifi(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Log.e(TAG, "isWifi: ConnectivityManager");
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == 1;
    }

    /**
     * 扫描周围可用WiFi
     */
    public boolean startScan() {
        return isWifiEnabled() && mWifiManager.startScan();
    }

    /**
     * 获取周围可用WiFi扫描结果
     */
    public List<ScanResult> getScanResults() {
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        if (scanResults != null && scanResults.size() > 0) {
            return filterScanResult(scanResults);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取周围信号强度大于-80的WiFi列表（Wifi强度为负数，值越大信号越好）
     */
    public List<ScanResult> getWifiScanList() throws InterruptedException {
        List<ScanResult> resList = new ArrayList<ScanResult>();
        if (mWifiManager.startScan()) {
            List<ScanResult> tmpList = mWifiManager.getScanResults();
            Thread.sleep(2000);
            if (tmpList != null && tmpList.size() > 0) {
//				resList = sortByLevel(tmpList);
                for (ScanResult scanResult : tmpList) {
                    if (scanResult.level > -80) {
                        resList.add(scanResult);
                    }
                }
            } else {
                System.err.println("扫描为空");
            }
        }
        return resList;
    }

    /**
     * 判断当前WiFi是否正确连接指定WiFi
     */
    public boolean isWifiConnected(String SSID) {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo != null && wifiInfo.getSSID().equals(SSID);
    }

    /**
     * 获取当前连接WiFi的SSID
     */
    public String getConnectedSSID() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo != null ? wifiInfo.getSSID().replaceAll("\"", "") : "";
    }

    /**
     * 连接WiFi
     */
    public boolean connectWifi(final String ssid, final String pwd, List<ScanResult> scanResults) throws InterruptedException {
        if (scanResults == null || scanResults.size() == 0) {
            return false;
        }

        //匹配SSID相同的WiFi
        ScanResult result = null;
        for (ScanResult tmpResult : scanResults) {
            if (tmpResult.SSID.equals(ssid)) {
                result = tmpResult;
                break;
            }
        }

        if (result == null) {
            return false;
        }

        if (isAdHoc(result)) {
            return false;
        }

        final String security = WIFI.CONFIG_SEC.getScanResultSecurity(result);
        final WifiConfiguration config = WIFI.getWifiConfiguration(mWifiManager, result, security);

        if (config == null) {
            //连接新WiFi
            boolean connResult;
            int numOpenNetworksKept = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
            String scanResultSecurity = WIFI.CONFIG_SEC.getScanResultSecurity(result);
            boolean isOpenNetwork = WIFI.CONFIG_SEC.isOpenNetwork(scanResultSecurity);
            if (isOpenNetwork) {
                connResult = WIFI.connectToNewNetwork(mContext, mWifiManager, result, null, numOpenNetworksKept);
            } else {
                connResult = WIFI.connectToNewNetwork(mContext, mWifiManager, result, pwd, numOpenNetworksKept);
            }
            return connResult;
        } else {
            final boolean isCurrentNetwork_ConfigurationStatus = config.status == WifiConfiguration.Status.CURRENT;
            final WifiInfo info = mWifiManager.getConnectionInfo();
            final boolean isCurrentNetwork_WifiInfo = info != null
                    && TextUtils.equals(info.getSSID(), result.SSID)
                    && TextUtils.equals(info.getBSSID(), result.BSSID);
            if (!isCurrentNetwork_ConfigurationStatus && !isCurrentNetwork_WifiInfo) {
                //连接已保存的WiFi
                String scanResultSecurity = WIFI.CONFIG_SEC.getScanResultSecurity(result);
                final WifiConfiguration wcg = WIFI.getWifiConfiguration(mWifiManager, result, scanResultSecurity);
                boolean connResult = false;
                if (wcg != null) {
                    connResult = WIFI.connectToConfiguredNetwork(mContext, mWifiManager, wcg, false);
                }
                return connResult;
            } else {
                //点击的是当前已连接的WiFi
                return true;
            }
        }
    }

    /**
     * 断开指定ID的网络
     */
    public boolean disconnectWifi(String SSID) {
        return mWifiManager.disableNetwork(getNetworkIdBySSID(SSID)) && mWifiManager.disconnect();
    }

    /**
     * 清除指定SSID的网络
     */
    public void clearWifiConfig(String SSID) {
        SSID = SSID.replace("\"", "");
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        if (wifiConfigurations != null && wifiConfigurations.size() > 0) {
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if (wifiConfiguration.SSID.replace("\"", "").contains(SSID)) {
                    mWifiManager.removeNetwork(wifiConfiguration.networkId);
                    mWifiManager.saveConfiguration();
                }
            }
        }
    }

    /**
     * 清除当前连接的WiFi网络
     */
    public void clearWifiConfig() {
        String SSID = mWifiManager.getConnectionInfo().getSSID().replace("\"", "");
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        if (wifiConfigurations != null && wifiConfigurations.size() > 0) {
            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
                if (wifiConfiguration.SSID.replace("\"", "").contains(SSID)) {
                    mWifiManager.removeNetwork(wifiConfiguration.networkId);
                    mWifiManager.saveConfiguration();
                }
            }
        }
    }

    private boolean isAdHoc(final ScanResult scanResult) {
        return scanResult.capabilities.contains("IBSS");
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
    public String getIPAddressFromHotspot() {
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            int address = dhcpInfo.gateway;
            return ((address & 0xFF)
                    + "." + ((address >> 8) & 0xFF)
                    + "." + ((address >> 16) & 0xFF)
                    + "." + ((address >> 24) & 0xFF));
        }
        return null;
    }

    /**
     * 创建WifiConfiguration对象 分为三种情况：1没有密码;2用wep加密;3用wpa加密
     */
    public WifiConfiguration CreateWifiInfo(String SSID, String Password,
                                            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExists(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) // WIFI_CIPHER_NO_PASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFI_CIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFI_CIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 添加一个网络并连接 传入参数：WIFI发生配置类WifiConfiguration
     */
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        return mWifiManager.enableNetwork(wcgID, true);
    }

    /**
     * 获取当前手机所连接的wifi信息
     */
    public WifiInfo getCurrentWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }

    /**
     * 获取指定WiFi信息
     */
    private WifiConfiguration isExists(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
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
     * 过滤WiFi扫描结果
     */
    private List<ScanResult> filterScanResult(List<ScanResult> scanResults) {
        List<ScanResult> result = new ArrayList<>();
        if (scanResults == null) {
            return result;
        }

        for (ScanResult scanResult : scanResults) {
            if (!TextUtils.isEmpty(scanResult.SSID) && scanResult.level > -80) {
                result.add(scanResult);
            }
        }

        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size(); j++) {
                //将搜索到的wifi根据信号强度从强到弱进行排序
                if (result.get(i).level > result.get(j).level) {
                    ScanResult temp = result.get(i);
                    result.set(i, result.get(j));
                    result.set(j, temp);
                }
            }
        }
        return result;
    }

}
