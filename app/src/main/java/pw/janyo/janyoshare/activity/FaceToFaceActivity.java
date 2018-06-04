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

package pw.janyo.janyoshare.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.Calendar;
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
import pw.janyo.janyoshare.classes.ConnectedSocket;
import pw.janyo.janyoshare.util.Constant;
import pw.janyo.janyoshare.util.socket.SocketUtil;
import pw.janyo.janyoshare.util.wifi.APUtil;
import pw.janyo.janyoshare.util.wifi.IPScanner;
import pw.janyo.janyoshare.util.wifi.WIFIUtil;
import pw.janyo.janyoshare.util.wifi.WiFiBroadcastReceiver;
import vip.mystery0.logs.Logs;
import vip.mystery0.tools.base.BaseActivity;

public class FaceToFaceActivity extends BaseActivity {
	private static final int WRITE_SETTINGS_PERMISSION_CODE = 233;
	private static final int SCAN_WIFI_PERMISSION_CODE = 244;
	private FloatingActionButton fab_receive;
	private FloatingActionButton fab_send;
	private ZLoadingDialog receiveDialog;
	private ZLoadingDialog sendDialog;
	private AlertDialog.Builder builder;
	private APUtil apUtil;
	private WIFIUtil wifiUtil;
	private SocketUtil socketUtil;

	public FaceToFaceActivity() {
		super(R.layout.activity_face_to_face);
	}

	@Override
	public void bindView() {
		super.bindView();
		fab_receive = findViewById(R.id.floatingActionButtonReceive);
		fab_send = findViewById(R.id.floatingActionButtonSend);
	}

	@Override
	public void initData() {
		super.initData();
		wifiUtil = new WIFIUtil(FaceToFaceActivity.this);

		receiveDialog = new ZLoadingDialog(FaceToFaceActivity.this)
				.setLoadingBuilder(Z_TYPE.STAR_LOADING)
				.setHintTextSize(16)
				.setCancelable(false)
				.setCanceledOnTouchOutside(false)
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
	}

	@Override
	public void monitor() {
		super.monitor();
		fab_receive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiUtil.isWifi())
					new AlertDialog.Builder(FaceToFaceActivity.this)
							.setTitle(" ")
							.setMessage(R.string.hint_direct_wifi)
							.setPositiveButton(R.string.action_direct_wifi, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									scanIP();
								}
							})
							.setNegativeButton(R.string.action_reconnect, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									requestLocationPermission();
								}
							})
							.show();
				else
					requestLocationPermission();
			}
		});
		fab_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiUtil.isWifi())
					new AlertDialog.Builder(FaceToFaceActivity.this)
							.setTitle(" ")
							.setMessage(R.string.hint_direct_wifi)
							.setPositiveButton(R.string.action_direct_wifi, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									readySocketServer();
								}
							})
							.setNegativeButton(R.string.action_new_hotspot, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									requestWriteSettings();
								}
							})
							.show();
				else
					requestWriteSettings();
			}
		});
	}

	private void readyToShare() {
		final Handler handler = new Handler();
		Observable.create(new ObservableOnSubscribe<Boolean>() {
			@Override
			public void subscribe(final ObservableEmitter<Boolean> subscriber) throws Exception {
				apUtil = new APUtil(FaceToFaceActivity.this);
				//开始检测现有的热点配置是否正确
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					apUtil.openAP(handler, new WifiManager.LocalOnlyHotspotCallback() {
						@Override
						public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
							super.onStarted(reservation);
							Logs.i("onStarted: ");
							subscriber.onComplete();
						}

						@Override
						public void onStopped() {
							super.onStopped();
							Logs.i("onStopped: ");
							subscriber.onComplete();
						}

						@Override
						public void onFailed(int reason) {
							super.onFailed(reason);
							Logs.i("onFailed: ");
							subscriber.onComplete();
						}
					});
				} else {
					apUtil.openAP(Constant.WIFI_SSID + '_' + pw.janyo.janyoshare.util.Settings.getNickName());
					subscriber.onComplete();
				}
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
						if (aBoolean) {//8.0以上
							sendDialog.dismiss();
							new AlertDialog.Builder(FaceToFaceActivity.this)
									.setTitle(" ")
									.setMessage("test")
									.setPositiveButton(R.string.action_go, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
											startActivity(intent);
										}
									})
									.setNegativeButton(android.R.string.cancel, null)
									.show();
						}
					}

					@Override
					public void onError(Throwable e) {
						sendDialog.dismiss();
						Logs.wtf( "onError: ", e);
					}

					@Override
					public void onComplete() {
						sendDialog.dismiss();
						readySocketServer();
					}
				});
	}

	private void readyToReceive() {
		Observable.create(new ObservableOnSubscribe<Boolean>() {
			@Override
			public void subscribe(ObservableEmitter<Boolean> subscriber) {
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
						Logs.i( "onSubscribe: start: " + Calendar.getInstance().getTimeInMillis());
						receiveDialog.setHintText(getString(R.string.hint_face_scan_hotspot))
								.show();
					}

					@Override
					public void onNext(Boolean aBoolean) {
					}

					@Override
					public void onError(Throwable e) {
						receiveDialog.dismiss();
						Logs.wtf( "onError: ", e);
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
			public void subscribe(ObservableEmitter<Boolean> subscriber) {
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
						Logs.wtf( "onError: ", e);
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
			public void subscribe(final ObservableEmitter<Boolean> subscriber) {
				WiFiBroadcastReceiver wiFiBroadcastReceiver = new WiFiBroadcastReceiver() {
					@Override
					public void onWifiConnected(String connectedSSID) {
						if (connectedSSID.equals(SSID)) {
							Logs.i( "onWifiConnected: ");
							subscriber.onNext(wifiUtil.getConnectedSSID().equals(SSID));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							subscriber.onComplete();
							unRegisterWiFiReceiver(this);
						}
					}
				};
				registerWiFiReceiver(wiFiBroadcastReceiver);
				if (!wifiUtil.connectWiFi(SSID, SSID)) {
					subscriber.onNext(false);
					subscriber.onComplete();
				}
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
						Logs.wtf( "onError: ", e);
					}

					@Override
					public void onComplete() {
						receiveDialog.dismiss();
						Logs.i( "onComplete: " + result);
						if (result)
							scanIP();
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

	private void scanIP() {
		IPScanner ipScanner = new IPScanner();
		ipScanner.startScan(FaceToFaceActivity.this, new IPScanner.OnScanListener() {
			@Override
			public void connected(ConnectedSocket connectedSocket) {
				Logs.i( "connected: " + connectedSocket);
				readySocket(connectedSocket);
			}

			@Override
			public void none() {
				Logs.i( "none: ");
			}
		});
	}

	private void readySocket(final ConnectedSocket connectedSocket) {
		Observable.create(new ObservableOnSubscribe<Boolean>() {
			@Override
			public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
				Logs.i( "readySocket: " + wifiUtil.getIPAddressFromHotspot());
				SocketUtil socketUtil = connectedSocket.socketUtil;
				Logs.i( "receiveMessage: " + socketUtil.receiveMessage());
				subscriber.onComplete();
			}
		})
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Boolean>() {
					@Override
					public void onSubscribe(Disposable d) {

					}

					@Override
					public void onNext(Boolean aBoolean) {

					}

					@Override
					public void onError(Throwable e) {

					}

					@Override
					public void onComplete() {
						Logs.i( "onComplete: end: " + Calendar.getInstance().getTimeInMillis());
					}
				});
	}

	private void readySocketServer() {
		Observable.create(new ObservableOnSubscribe<Boolean>() {
			@Override
			public void subscribe(ObservableEmitter<Boolean> subscriber) throws Exception {
				if (wifiUtil.isWifi())
					Logs.i( "subscribe: " + wifiUtil.getIPAddressFromHotspot());
				else
					Logs.i( "subscribe: " + apUtil.getHotspotLocalIpAddress());
				if (socketUtil == null)
					socketUtil = new SocketUtil();
				socketUtil.accept();
				socketUtil.sendMessage(SocketUtil.VERIFY_MESSAGE);
				subscriber.onComplete();
			}
		})
				.subscribeOn(Schedulers.newThread())
				.unsubscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Boolean>() {
					@Override
					public void onSubscribe(Disposable d) {
						sendDialog.setHintText(getString(R.string.hint_face_wait_connect));
						sendDialog.dismiss();
						sendDialog.show();
					}

					@Override
					public void onNext(Boolean aBoolean) {
					}

					@Override
					public void onError(Throwable e) {
						sendDialog.dismiss();
						Logs.wtf( "onError: ", e);
					}

					@Override
					public void onComplete() {
						sendDialog.dismiss();
					}
				});
	}

	private void registerWiFiReceiver(WiFiBroadcastReceiver wiFiBroadcastReceiver) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(wiFiBroadcastReceiver, intentFilter);
	}

	private void unRegisterWiFiReceiver(WiFiBroadcastReceiver wiFiBroadcastReceiver) {
		unregisterReceiver(wiFiBroadcastReceiver);
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
