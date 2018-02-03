package pw.janyo.janyoshare.util.wifi;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.classes.ConnectedSocket;
import pw.janyo.janyoshare.util.socket.SocketUtil;
import vip.mystery0.tools.logs.Logs;

/**
 * Created by kalshen on 2017/7/5 0005.
 * ip 扫描类
 */

public class IPScanner {
    private static final String TAG = "IPScanner";

    /**
     * 获取局域网中的 存在的ip地址及对应的mac
     */
    public void startScan(final Context context, final OnScanListener onScanListener) {
        final ZLoadingDialog scanDialog = new ZLoadingDialog(context);
        scanDialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)
                .setHintTextSize(16)
                .setLoadingColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setHintTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        //局域网内存在的ip集合
        final List<ConnectedSocket> connectedSocketList = new ArrayList<>();
        final Map<String, String> map = new HashMap<>();

        Observable.create(new ObservableOnSubscribe<Map<String, String>>() {
            @Override
            public void subscribe(ObservableEmitter<Map<String, String>> subscriber) throws Exception {
                //获取本机所在的局域网地址
                String hostIP = getHostIP();
                Logs.i(TAG, "startScan: " + hostIP);
                int lastIndexOf = hostIP.lastIndexOf(".");
                final String substring = hostIP.substring(0, lastIndexOf + 1);
                DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0, 0);
                DatagramSocket socket = new DatagramSocket();
                int position = 1;
                while (position < 255) {
                    datagramPacket.setAddress(InetAddress.getByName(substring + String.valueOf(position)));
                    socket.send(datagramPacket);
                    position++;
                    if (position == 125) {//分两段掉包，一次性发的话，达到236左右，会耗时3秒左右再往下发
                        socket.close();
                        socket = new DatagramSocket();
                    }
                }
                socket.close();
                /*
                 * 执行 cat命令 查找android 设备arp表
                 * arp表 包含ip地址和对应的mac地址
                 */
                Process exec = Runtime.getRuntime().exec("cat proc/net/arp");
                InputStream is = exec.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains("00:00:00:00:00:00") && !line.contains("IP")) {
                        String[] split = line.split("\\s+");
                        Map<String, String> tempMap = new HashMap<>();
                        map.put(split[0], split[3]);
                        tempMap.put(split[0], split[3]);
                        subscriber.onNext(tempMap);
                    }
                }
                subscriber.onComplete();
            }
        })
                .doAfterNext(new Consumer<Map<String, String>>() {
                    @Override
                    public void accept(final Map<String, String> stringStringMap) throws Exception {
                        final String key = stringStringMap.keySet().toArray()[0].toString();
                        final boolean[] isFinish = {false};
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    SocketUtil socketUtil = new SocketUtil(key);
                                    if (socketUtil.connect()) {
                                        ConnectedSocket connectedSocket = new ConnectedSocket();
                                        connectedSocket.host = key;
                                        connectedSocket.mac = stringStringMap.get(key);
                                        connectedSocket.socketUtil = socketUtil;
                                        connectedSocketList.add(connectedSocket);
                                        isFinish[0] = true;
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }, 0);
                        Thread.sleep(2000);
                        if (!isFinish[0])
                            timer.cancel();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map<String, String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        scanDialog.show();
                    }

                    @Override
                    public void onNext(Map<String, String> stringStringMap) {
                        String key = stringStringMap.keySet().toArray()[0].toString();
                        Logs.i(TAG, "onNext: host: " + key);
                        Logs.i(TAG, "onNext: mac: " + stringStringMap.get(key));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logs.wtf(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        scanDialog.dismiss();
                        Logs.i(TAG, "onComplete: " + map);
                        if (connectedSocketList.size() > 1)
                            Logs.i(TAG, "onComplete: 显示对话框");
                        else if (connectedSocketList.size() == 1)
                            onScanListener.connected(connectedSocketList.get(0));
                        else
                            onScanListener.none();
                    }
                });
    }

    /**
     * 获取本机 ip地址
     */
    private String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Logs.wtf(TAG, "getHostIP: ", e);
        }
        return hostIp;
    }

    public interface OnScanListener {
        void connected(ConnectedSocket connectedSocket);

        void none();
    }
}