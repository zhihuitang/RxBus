package com.othershe.rxbus;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private Button mBtn1;
    private Button mBtn2;
    private TextView mTv;

    private NetworkChangeReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn1 = (Button) findViewById(R.id.btn1);
        mBtn2 = (Button) findViewById(R.id.btn2);
        mTv = (TextView) findViewById(R.id.tv);

        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.getInstance().post("1024");
            }
        });

        mBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RxBus.getInstance().post(2048);
                    }
                }).start();
            }
        });

        doSubscribe();

        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mReceiver = new NetworkChangeReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    private void doSubscribe() {
        Subscription subscription1 = RxBus.getInstance()
                .tObservable(String.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mTv.setText("事件内容：" + s);
                        Log.d("RxBus", "got event: " + s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });

        Subscription subscription2 = RxBus.getInstance()
                .doSubscribe(Integer.class, new Action1<Integer>() {
                    @Override
                    public void call(Integer s) {
                        mTv.setText("事件内容：" + s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });

        RxBus.getInstance().addSubscription(this, subscription1);
        RxBus.getInstance().addSubscription(this, subscription2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        RxBus.getInstance().unSubscribe(this);
    }
}
