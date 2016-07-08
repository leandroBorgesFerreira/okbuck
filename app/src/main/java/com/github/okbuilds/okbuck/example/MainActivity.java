package com.github.okbuilds.okbuck.example;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.example.hellojni.HelloJni;
import com.github.okbuilds.okbuck.example.common.Calc;
import com.github.okbuilds.okbuck.example.common.CalcMonitor;
import com.github.okbuilds.okbuck.example.common.IMyAidlInterface;
import com.github.okbuilds.okbuck.example.dummylibrary.DummyActivity;
import com.github.okbuilds.okbuck.example.dummylibrary.DummyAndroidClass;
import com.github.okbuilds.okbuck.example.javalib.DummyJavaClass;
import com.github.piasy.rxscreenshotdetector.RxScreenshotDetector;
import com.promegu.xlog.base.XLog;
import javax.inject.Inject;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@XLog
public class MainActivity extends AppCompatActivity {
    @Inject
    DummyJavaClass mDummyJavaClass;
    @Inject
    DummyAndroidClass mDummyAndroidClass;
    IMyAidlInterface mIMyAidlInterface;

    private Unbinder mUnbinder;

    @BindView(R2.id.mTextView)
    TextView mTextView;

    @BindView(R2.id.mTextView2)
    TextView mTextView2;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIMyAidlInterface = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);

        DummyComponent component = DaggerDummyComponent.builder().build();
        component.inject(this);

        mTextView.setText(
                String.format("%s %s, --from %s.", getString(R.string.dummy_library_android_str),
                        mDummyAndroidClass.getAndroidWord(this), mDummyJavaClass.getJavaWord()));

        mTextView2.setText(mTextView2.getText()
                + "\n\n"
                + HelloJni.stringFromJNI()
                + "\n\n"
                + FlavorLogger.log(this));

        if (BuildConfig.CAN_JUMP) {
            mTextView.setOnClickListener(v -> {
                //startActivity(new Intent(MainActivity.this, CollapsingAppBarActivity.class));
                startActivity(new Intent(MainActivity.this, DummyActivity.class));
            });
        }

        Log.d("test", "1 + 2 = " + new Calc(new CalcMonitor(this)).add(1, 2));

        RxScreenshotDetector.start(getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String path) {
                        mTextView.setText(mTextView.getText() + "\nScreenshot: " + path);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }
}
