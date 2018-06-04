/*
 * Created by Mystery0 on 18-2-10 下午4:44.
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
 * Last modified 18-2-10 下午4:44
 */

package pw.janyo.janyoshare.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.util.Settings;
import vip.mystery0.dirManager.DirManager;
import vip.mystery0.tools.base.BaseActivity;

public class DirManagerActivity extends BaseActivity {
	private DirManager dirManager;
	private Button buttonOk;
	private Button buttonCancel;

	public DirManagerActivity() {
		super(R.layout.activity_dir_manager);
	}

	@Override
	public void bindView() {
		super.bindView();
		dirManager = findViewById(R.id.dirManager);
		buttonOk = findViewById(R.id.button_ok);
		buttonCancel = findViewById(R.id.button_cancel);
	}

	@Override
	public void initData() {
		super.initData();
		setTitle(" ");

		dirManager.setCurrentPath(Settings.getCustomExportDir());
	}

	@Override
	public void monitor() {
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Settings.setCustomExportDir(dirManager.getCurrentPath());
				finish();
			}
		});
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
