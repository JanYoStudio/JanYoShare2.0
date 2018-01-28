package pw.janyo.janyoshare.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.util.Constant;
import pw.janyo.janyoshare.util.wifi.APUtil;
import pw.janyo.janyoshare.util.wifi.WIFIUtil;
import vip.mystery0.tools.logs.Logs;

public class FaceToFaceActivity extends AppCompatActivity {
    private static final String TAG = "FaceToFaceActivity";
    private static final int WRITE_SETTINGS_PERMISSION_CODE = 233;
    private static final int SCAN_WIFI_PERMISSION_CODE = 244;
    private ZLoadingDialog receiveDialog;
    private ZLoadingDialog sendDialog;
    private AlertDialog.Builder builder;
    private APUtil apUtil;
    private WIFIUtil wifiUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_to_face);

        receiveDialog = new ZLoadingDialog(FaceToFaceActivity.this)
                .setLoadingBuilder(Z_TYPE.STAR_LOADING)
                .setHintTextSize(16)
//                .setCancelable(false)
//                .setCanceledOnTouchOutside(false)
                .setLoadingColor(ContextCompat.getColor(FaceToFaceActivity.this, R.color.colorAccent))
                .setHintTextColor(ContextCompat.getColor(FaceToFaceActivity.this, R.color.colorAccent));

        sendDialog = new ZLoadingDialog(FaceToFaceActivity.this)
                .setLoadingBuilder(Z_TYPE.STAR_LOADING)
                .setHintText(getString(R.string.hint_face_create_hotspot))
                .setHintTextSize(16)
//                .setCancelable(false)
//                .setCanceledOnTouchOutside(false)
                .setLoadingColor(ContextCompat.getColor(FaceToFaceActivity.this, R.color.colorAccent))
                .setHintTextColor(ContextCompat.getColor(FaceToFaceActivity.this, R.color.colorAccent));

        FloatingActionButton fab_receive = findViewById(R.id.floatingActionButtonReceive);
        FloatingActionButton fab_send = findViewById(R.id.floatingActionButtonSend);

        fab_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i(TAG, "onClick: receive");
                requestLocationPermission();
            }
        });
        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i(TAG, "onClick: send");
                requestWriteSettings();
            }
        });
    }

    private void readyToShare() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
                apUtil = new APUtil(FaceToFaceActivity.this);
                apUtil.openAP(Constant.WIFI_SSID + '_' + pw.janyo.janyoshare.util.Settings.getNickName());
                Logs.i(TAG, "readyToShare: " + apUtil.getHotspotLocalIpAddress());
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        sendDialog.show();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        sendDialog.dismiss();
                        Logs.wtf(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        sendDialog.setHintText("waiting……");
//                        sendDialog.dismiss();
                    }
                });
    }

    private void readyToReceive() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
                wifiUtil = new WIFIUtil(FaceToFaceActivity.this);
                if (!wifiUtil.isWifi())
                    wifiUtil.openWifi();
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        receiveDialog.setHintText(getString(R.string.hint_face_scan_hotspot))
                                .show();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        receiveDialog.dismiss();
                        Logs.wtf(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        if (wifiUtil.isWifiEnabled())
                            scanWIFI();
                    }
                });
    }

    private void scanWIFI() {
        final List<ScanResult> scanResultList = new ArrayList<>();
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
                scanResultList.clear();
                scanResultList.addAll(wifiUtil.scanWIFI());
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logs.wtf(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        setAlertDialog(scanResultList);
                        builder.show();
                    }
                });
    }

    private void connectWIFI(final String SSID) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
                if (!wifiUtil.connectWiFi(SSID))
                    subscriber.onNext(false);
                else {
                    Thread.sleep(1000);//睡眠1秒获取信息
                    subscriber.onNext(wifiUtil.getConnectedSSID().equals(SSID));
                }
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    private boolean result = false;

                    @Override
                    public void onSubscribe(Disposable d) {
                        receiveDialog.setHintText(getString(R.string.hint_face_connect_hotspot));
                        receiveDialog.dismiss();
                        receiveDialog.show();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        result = aBoolean;
                    }

                    @Override
                    public void onError(Throwable e) {
                        receiveDialog.dismiss();
                        Logs.wtf(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        receiveDialog.dismiss();
                        Logs.i(TAG, "onComplete: " + result);
                    }
                });
    }

    private void setAlertDialog(final List<ScanResult> scanResultList) {
        final String[] items = new String[scanResultList.size()];
        for (int i = 0; i < scanResultList.size(); i++) {
            items[i] = scanResultList.get(i).SSID;
        }
        if (builder == null)
            builder = new AlertDialog.Builder(FaceToFaceActivity.this)
                    .setTitle(R.string.title_dialog_select_wifi)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            connectWIFI(items[which]);
                        }
                    })
                    .setPositiveButton(R.string.action_refresh, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            scanWIFI();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        else
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    connectWIFI(items[which]);
                }
            });
    }

    private void requestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!Settings.System.canWrite(FaceToFaceActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, WRITE_SETTINGS_PERMISSION_CODE);
                Toast.makeText(FaceToFaceActivity.this, R.string.hint_permission_modify_settings, Toast.LENGTH_LONG)
                        .show();
            } else
                readyToShare();
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE
                }, SCAN_WIFI_PERMISSION_CODE);
            else
                readyToReceive();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_SETTINGS_PERMISSION_CODE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (Settings.System.canWrite(FaceToFaceActivity.this))
                    readyToShare();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SCAN_WIFI_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    readyToReceive();
                else
                    Toast.makeText(FaceToFaceActivity.this, R.string.hint_permission_location, Toast.LENGTH_LONG)
                            .show();
                break;
        }
    }
}
