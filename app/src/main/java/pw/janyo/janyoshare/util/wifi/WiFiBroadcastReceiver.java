/*
 * Created by Mystery0 on 18-2-10 下午4:00.
 * Copyright (c) 2018. All Rights reserved.
 *
 *                    =====================================================
 *                    =                                                   =
 *                    =                       _oo0oo_                     =
 *                    =                      o8888888o                    =
 *                    =                      88" . "88                    =
 *                    =                      (| -_- |)                    =
 *                    =                      0\  =  /0                    =
 *                    =                    ___/`---'\___                  =
 *                    =                  .' \\|     |# '.                 =
 *                    =                 / \\|||  :  |||# \                =
 *                    =                / _||||| -:- |||||- \              =
 *                    =               |   | \\\  -  #/ |   |              =
 *                    =               | \_|  ''\---/''  |_/ |             =
 *                    =               \  .-\__  '-'  ___/-. /             =
 *                    =             ___'. .'  /--.--\  `. .'___           =
 *                    =          ."" '<  `.___\_<|>_/___.' >' "".         =
 *                    =         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       =
 *                    =         \  \ `_.   \_ __\ /__ _/   .-` /  /       =
 *                    =     =====`-.____`.___ \_____/___.-`___.-'=====    =
 *                    =                       `=---='                     =
 *                    =     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   =
 *                    =                                                   =
 *                    =               佛祖保佑         永无BUG              =
 *                    =                                                   =
 *                    =====================================================
 *
 * Last modified 18-2-10 下午4:00
 */

package pw.janyo.janyoshare.util.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public abstract class WiFiBroadcastReceiver extends BroadcastReceiver {
    public void onWifiEnabled() {
    }

    public void onWifiDisabled() {
    }

    public void onScanResultsAvailable(List<ScanResult> scanResults) {
    }

    public void onWifiConnected(String connectedSSID) {
    }

    public void onWifiDisconnected() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    //监听WiFi开启/关闭事件
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        //WiFi已开启
                        onWifiEnabled();
                    } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                        //WiFi已关闭
                        onWifiDisabled();
                    }
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    WIFIUtil wifiMgr = new WIFIUtil(context);
                    List<ScanResult> scanResults = wifiMgr.scanWIFI();
                    if (wifiMgr.isWifiEnabled() && scanResults != null && scanResults.size() > 0) {
                        //成功扫描
                        onScanResultsAvailable(scanResults);
                    }
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    //网络状态改变的广播
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info != null) {
                        if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                            //WiFi已连接
                            String connectedSSID = new WIFIUtil(context).getConnectedSSID();
                            onWifiConnected(connectedSSID);
                        } else if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                            //WiFi已断开连接
                            onWifiDisconnected();
                        }
                    }
                    break;
            }
        }
    }
}
