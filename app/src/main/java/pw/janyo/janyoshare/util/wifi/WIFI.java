/*
 * Wifi Connecter
 * 
 * Copyright (c) 2011 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/

package pw.janyo.janyoshare.util.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.Comparator;
import java.util.List;

public class WIFI {

    static final ConfigurationSecurities CONFIG_SEC = new ConfigurationSecurities();

    private static final String TAG = "WIFI";

    /**
     * Change the password of an existing configured network and connect to it
     */
    public static boolean changePasswordAndConnect(final Context context, final WifiManager wifiManager, final WifiConfiguration config, final String newPassword, final int numOpenNetworksKept) {
        CONFIG_SEC.setupSecurity(config, CONFIG_SEC.getWifiConfigurationSecurity(config), newPassword);
        final int networkId = wifiManager.updateNetwork(config);
        if (networkId == -1) {
            // Update failed.
            return false;
        }
        // Force the change to apply.
        wifiManager.disconnect();
        return connectToConfiguredNetwork(context, wifiManager, config, true);
    }

    /**
     * Configure a network, and connect to it.
     * 
     * @param password   Password for secure network or is ignored.
     */
    public static boolean connectToNewNetwork(final Context context, final WifiManager wifiManager, final ScanResult scanResult, final String password, final int numOpenNetworksKept) {
        final String security = CONFIG_SEC.getScanResultSecurity(scanResult);

        if (CONFIG_SEC.isOpenNetwork(security)) {
            checkForExcessOpenNetworkAndSave(wifiManager, numOpenNetworksKept);
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = convertToQuotedString(scanResult.SSID);
        config.BSSID = scanResult.BSSID;
        CONFIG_SEC.setupSecurity(config, security, password);

        int id = -1;
        try {
            id = wifiManager.addNetwork(config);
        } catch (NullPointerException e) {
            Log.e(TAG, "Weird!! Really!! What's wrong??", e);
            // Weird!! Really!!
            // This exception is reported by user to Android Developer Console(https://market.android.com/publish/Home)
        }
        if (id == -1) {
            return false;
        }

        if (!wifiManager.saveConfiguration()) {
            return false;
        }

        config = getWifiConfiguration(wifiManager, config, security);
        return config != null && connectToConfiguredNetwork(context, wifiManager, config, true);
    }

    /**
     * Connect to a configured network.
     */
    @SuppressWarnings("deprecation")
    public static boolean connectToConfiguredNetwork(final Context context, final WifiManager wifiManager, WifiConfiguration config, boolean reAssociate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return connectToConfiguredNetworkV23(context, wifiManager, config, reAssociate);
        }
        final String security = CONFIG_SEC.getWifiConfigurationSecurity(config);

        int oldPri = config.priority;
        // Make it the highest priority.
        int newPri = getMaxPriority(wifiManager) + 1;
        if (newPri > MAX_PRIORITY) {
            newPri = shiftPriorityAndSave(wifiManager);
            config = getWifiConfiguration(wifiManager, config, security);
            if (config == null) {
                return false;
            }
        }

        // Set highest priority to this configured network
        config.priority = newPri;
        int networkId = wifiManager.updateNetwork(config);
        if (networkId == -1) {
            return false;
        }

        // Do not disable others
        if (!wifiManager.enableNetwork(networkId, false)) {
            config.priority = oldPri;
            return false;
        }

        if (!wifiManager.saveConfiguration()) {
            config.priority = oldPri;
            return false;
        }

        // We have to retrieve the WifiConfiguration after save.
        config = getWifiConfiguration(wifiManager, config, security);
        if (config == null) {
            return false;
        }

        ReEnableAllApsWhenNetworkStateChanged.schedule(context);

        // Disable others, but do not save.
        // Just to force the WifiManager to connect to it.
        return wifiManager.enableNetwork(config.networkId, true) && (reAssociate ? wifiManager.reassociate() : wifiManager.reconnect());
    }

    private static boolean connectToConfiguredNetworkV23(final Context context, final WifiManager wifiManager, WifiConfiguration config, boolean reAssociate) {
        return wifiManager.enableNetwork(config.networkId, true) && (reAssociate ? wifiManager.reassociate() : wifiManager.reconnect());
    }

    private static void sortByPriority(final List<WifiConfiguration> configurations) {
        java.util.Collections.sort(configurations, new Comparator<WifiConfiguration>() {

            @Override
            public int compare(WifiConfiguration object1,
                               WifiConfiguration object2) {
                return object1.priority - object2.priority;
            }
        });
    }

    /**
     * Ensure no more than numOpenNetworksKept open networks in configuration list.
     *
     * @return Operation succeed or not.
     */
    private static boolean checkForExcessOpenNetworkAndSave(final WifiManager wifiManager, final int numOpenNetworksKept) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        sortByPriority(configurations);

        boolean modified = false;
        int tempCount = 0;
        for (int i = configurations.size() - 1; i >= 0; i--) {
            final WifiConfiguration config = configurations.get(i);
            if (CONFIG_SEC.isOpenNetwork(CONFIG_SEC.getWifiConfigurationSecurity(config))) {
                tempCount++;
                if (tempCount >= numOpenNetworksKept) {
                    modified = true;
                    wifiManager.removeNetwork(config.networkId);
                }
            }
        }
        return !modified || wifiManager.saveConfiguration();
    }

    private static final int MAX_PRIORITY = 99999;

    private static int shiftPriorityAndSave(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        sortByPriority(configurations);
        final int size = configurations.size();
        for (int i = 0; i < size; i++) {
            final WifiConfiguration config = configurations.get(i);
            config.priority = i;
            wifiManager.updateNetwork(config);
        }
        wifiManager.saveConfiguration();
        return size;
    }

    private static int getMaxPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int pri = 0;
        for (final WifiConfiguration config : configurations) {
            if (config.priority > pri) {
                pri = config.priority;
            }
        }
        return pri;
    }

    private static final String BSSID_ANY = "any";

    public static WifiConfiguration getWifiConfiguration(final WifiManager wifiManager, final ScanResult hotsopt, String hotspotSecurity) {
        final String ssid = convertToQuotedString(hotsopt.SSID);
        if (ssid.length() == 0) {
            return null;
        }

        final String BSSID = hotsopt.BSSID;
        if (BSSID == null) {
            return null;
        }

        if (hotspotSecurity == null) {
            hotspotSecurity = CONFIG_SEC.getScanResultSecurity(hotsopt);
        }

        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        if (configurations == null) {
            return null;
        }

        for (final WifiConfiguration config : configurations) {
            if (config.SSID == null || !ssid.equals(config.SSID)) {
                continue;
            }
            if (config.BSSID == null || BSSID_ANY.equals(config.BSSID) || BSSID.equals(config.BSSID)) {
                final String configSecurity = CONFIG_SEC.getWifiConfigurationSecurity(config);
                if (hotspotSecurity.equals(configSecurity)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static WifiConfiguration getWifiConfiguration(final WifiManager wifiManager, final WifiConfiguration configToFind, String security) {
        final String ssid = configToFind.SSID;
        if (ssid.length() == 0) {
            return null;
        }

        final String bssid = configToFind.BSSID;


        if (security == null) {
            security = CONFIG_SEC.getWifiConfigurationSecurity(configToFind);
        }

        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();

        for (final WifiConfiguration config : configurations) {
            if (config.SSID == null || !ssid.equals(config.SSID)) {
                continue;
            }
            if (config.BSSID == null || BSSID_ANY.equals(config.BSSID) || bssid == null || bssid.equals(config.BSSID)) {
                final String configSecurity = CONFIG_SEC.getWifiConfigurationSecurity(config);
                if (security.equals(configSecurity)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        final int lastPos = string.length() - 1;
        if (lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }
        return "\"" + string + "\"";
    }

}
