/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arialyy.simple.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.Speed;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

public class SingleTaskActivity extends BaseActivity<ActivitySingleBinding> {
  public static final int DOWNLOAD_PRE = 0x01;
  public static final int DOWNLOAD_STOP = 0x02;
  public static final int DOWNLOAD_FAILE = 0x03;
  public static final int DOWNLOAD_CANCEL = 0x04;
  public static final int DOWNLOAD_RESUME = 0x05;
  public static final int DOWNLOAD_COMPLETE = 0x06;
  public static final int DOWNLOAD_RUNNING = 0x07;

  private static final String DOWNLOAD_URL =
      //"http://kotlinlang.org/docs/kotlin-docs.pdf";
      //"https://atom-installer.github.com/v1.13.0/AtomSetup.exe?s=1484074138&ext=.exe";
      //"http://static.gaoshouyou.com/d/22/94/822260b849944492caadd2983f9bb624.apk";
      //不支持断点的链接
      //"http://ox.konsung.net:5555/ksdc-web/download/downloadFile/?fileName=ksdc_1.0.2.apk&rRange=0-";
      "http://172.18.104.50:8080/download/_302turn";
  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.size) TextView mSize;
  @Bind(R.id.toolbar) Toolbar toolbar;
  @Bind(R.id.speed) TextView mSpeed;
  @Bind(R.id.speeds) RadioGroup mRg;
  private DownloadEntity mEntity;
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals(Aria.ACTION_START)) {
        L.d("START");
      }
    }
  };

  private Handler mUpdateHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case DOWNLOAD_RUNNING:
          DownloadTask task = (DownloadTask) msg.obj;
          long current = task.getCurrentProgress();
          long len = task.getFileSize();
          if (len == 0) {
            mPb.setProgress(0);
          } else {
            mPb.setProgress((int) ((current * 100) / len));
          }
          mSpeed.setText(CommonUtil.formatFileSize(task.getSpeed()) + "/s");
          break;
        case DOWNLOAD_PRE:
          mSize.setText(CommonUtil.formatFileSize((Long) msg.obj));
          setBtState(false);
          //mStart.setText("暂停");
          break;
        case DOWNLOAD_FAILE:
          Toast.makeText(SingleTaskActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
          setBtState(true);
          break;
        case DOWNLOAD_STOP:
          Toast.makeText(SingleTaskActivity.this, "暂停下载", Toast.LENGTH_SHORT).show();
          mStart.setText("恢复");
          setBtState(true);
          break;
        case DOWNLOAD_CANCEL:
          mPb.setProgress(0);
          Toast.makeText(SingleTaskActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
          mStart.setText("开始");
          setBtState(true);
          break;
        case DOWNLOAD_RESUME:
          //Toast.makeText(SingleTaskActivity.this,
          //    "恢复下载，恢复位置 ==> " + CommonUtil.formatFileSize((Long) msg.obj), Toast.LENGTH_SHORT)
          //    .show();
          mStart.setText("暂停");
          setBtState(false);
          break;
        case DOWNLOAD_COMPLETE:
          mPb.setProgress(100);
          Toast.makeText(SingleTaskActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
          mStart.setText("重新开始？");
          mCancel.setEnabled(false);
          setBtState(true);
          break;
      }
    }
  };

  /**
   * 设置start 和 stop 按钮状态
   */
  private void setBtState(boolean state) {
    mStart.setEnabled(state);
    mStop.setEnabled(!state);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
  }

  @Override protected void onResume() {
    super.onResume();
    Aria.download(this).addSchedulerListener(new MySchedulerListener());
    //registerReceiver(mReceiver, getModule(DownloadModule.class).getDownloadFilter());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    //unregisterReceiver(mReceiver);
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_single;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setSupportActionBar(toolbar);
    toolbar.setTitle("单任务下载");
    init();
    Aria.get(this).openBroadcast(true);
  }

  private void init() {
    if (Aria.download(this).taskExists(DOWNLOAD_URL)) {
      DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
      int p = (int) (target.getCurrentProgress() * 100 / target.getFileSize());
      mPb.setProgress(p);
    }
    mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case 1:
            Aria.get(this).setMaxSpeed(Speed.KB_256);
            break;
          case 2:
            Aria.get(this).setMaxSpeed(Speed.KB_512);
            break;
          case 3:
            Aria.get(this).setMaxSpeed(Speed.MB_1);
            break;
          case 4:
            Aria.get(this).setMaxSpeed(Speed.MB_2);
            break;
          case 5:
            Aria.get(this).setMaxSpeed(Speed.MAX);
            break;
        }
        stop();
        new Handler().postDelayed(new Runnable() {
          @Override public void run() {
            start();
          }
        }, 2000);
      }
    });
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        String text = ((TextView) view).getText().toString();
        if (text.equals("重新开始？") || text.equals("开始")) {
          start();
        } else if (text.equals("恢复")) {
          resume();
        }
        break;
      case R.id.stop:
        stop();
        break;
      case R.id.cancel:
        cancel();
        break;
    }
  }

  private void resume() {
    Aria.download(this).load(DOWNLOAD_URL).resume();
  }

  private void start() {
    Aria.download(this)
        .load(DOWNLOAD_URL)
        .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")
        .start();
  }

  private void stop() {
    Aria.download(this).load(DOWNLOAD_URL).pause();
  }

  private void cancel() {
    Aria.download(this).load(DOWNLOAD_URL).cancel();
  }

  private class MySchedulerListener extends Aria.DownloadSchedulerListener {

    @Override public void onNoSupportBreakPoint(DownloadTask task) {
      super.onNoSupportBreakPoint(task);
      T.showShort(SingleTaskActivity.this, "该下载链接不支持断点");
    }

    @Override public void onTaskStart(DownloadTask task) {
      mUpdateHandler.obtainMessage(DOWNLOAD_PRE, task.getDownloadEntity().getFileSize())
          .sendToTarget();
    }

    @Override public void onTaskResume(DownloadTask task) {
      super.onTaskResume(task);
      mUpdateHandler.obtainMessage(DOWNLOAD_PRE, task.getFileSize()).sendToTarget();
    }

    @Override public void onTaskStop(DownloadTask task) {
      mUpdateHandler.sendEmptyMessage(DOWNLOAD_STOP);
    }

    @Override public void onTaskCancel(DownloadTask task) {
      mUpdateHandler.sendEmptyMessage(DOWNLOAD_CANCEL);
    }

    @Override public void onTaskFail(DownloadTask task) {
      mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAILE);
    }

    @Override public void onTaskComplete(DownloadTask task) {
      mUpdateHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
    }

    @Override public void onTaskRunning(DownloadTask task) {
      mUpdateHandler.obtainMessage(DOWNLOAD_RUNNING, task).sendToTarget();
    }
  }
}