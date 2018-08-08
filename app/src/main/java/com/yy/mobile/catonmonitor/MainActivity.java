package com.yy.mobile.catonmonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yy.mobile.catonmonitorsdk.monitor.BlockMonitor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BlockMonitor.init("123456", "测试app", "v1.0.0", true);

        findViewById(R.id.btn_elapsed_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    test();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void test() throws InterruptedException {
        test1();
        test2();
        test3();
        test4();
    }

    private void test1() throws InterruptedException {
        Thread.sleep(200);
    }

    private void test2() throws InterruptedException {
        Thread.sleep(20);
    }

    private void test3() throws InterruptedException {
        Thread.sleep(800);
    }

    private void test4() throws InterruptedException {
        Thread.sleep(10);
    }
}
