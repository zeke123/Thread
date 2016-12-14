package com.zhoujian.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity
{
    @InjectView(R.id.download)
    Button mDownload;
    @InjectView(R.id.img)
    ImageView mImg;
    private MyDownLoadAsynctask mAsynctask;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        clickEvent();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("正在下载，请稍后...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
    }
    private void clickEvent()
    {
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAsynctask = new MyDownLoadAsynctask();
                mAsynctask.execute("http://img04.muzhiwan.com/2015/06/16/upload_557fd293326f5.jpg");

            }
        });
    }

    class MyDownLoadAsynctask extends AsyncTask<String, Integer, Bitmap>
    {

        /**
         * 在主线程中执行，异步任务执行之前会调用，一般用于做一些准备工作
         */

        @Override
        protected void onPreExecute()
        {
            Toast.makeText(MainActivity.this, "异步任务开始执行下载", Toast.LENGTH_SHORT).show();
            progressDialog.show();
        }

        /**
         * 在线程池中执行，用于执行异步任务
         * @param strings
         * @return
         */
        @Override
        protected Bitmap doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(5000);
                int code = connection.getResponseCode();
                if (code == 200) {
                    InputStream is = connection.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int length = -1;
                    int progress = 0;
                    int count = connection.getContentLength();
                    byte[] bytes = new byte[1024];
                    while ((length = is.read(bytes)) != -1) {
                        progress += length;
                        if (count == 0) {
                            publishProgress(-1);
                        } else {
                            //进度值改变通知，会调用onProgressUpdate（）方法
                            publishProgress((int) ((float) progress / count * 100));
                        }
                        if (isCancelled()) {
                            return null;
                        }
                        Thread.sleep(50);
                        bos.write(bytes, 0, length);
                    }
                    return BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        /**
         * 异步任务执行之后，会调用该方法，此方法会返回后台任务的返回值，即doInBackground的返回值
         * @param bitmap
         */
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            Toast.makeText(MainActivity.this, "图片下载完成", Toast.LENGTH_SHORT).show();
            mImg.setImageBitmap(bitmap);
            progressDialog.dismiss();
        }
        /**
         * 在主线程中执行，当后台任务的执行进度发生改变时会调用这个方法
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            int progress = values[0];
            if (progress != -1) {
                progressDialog.setProgress(progress);
            }
        }
    }
}