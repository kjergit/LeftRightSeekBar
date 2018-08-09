package com.example.simon.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mBtnOpenRange;

    Button mButton;
    CenterSeekBar mSeekBar;
    ExpSwitchView mEXPSwitchView;
    boolean isCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSeekBar = (CenterSeekBar) findViewById(R.id.seekbar);
        mSeekBar.setCenterModeEnable(true);

        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBar.setProgress(mSeekBar.getProgress() + 1);
                mButton.setText(String.valueOf(mSeekBar.getProgress()));
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new CenterSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onFinished(int progress) {
                Log.e("aaa", ".... progress =" + progress);
            }

            @Override
            public void onProgress(int progress) {
                mButton.setText(String.valueOf(progress));
                Log.e("aaa", ".... onProgress =" + progress);
            }
        });

        mEXPSwitchView = findViewById(R.id.exp_view);

        final Button mButtonBg = findViewById(R.id.button2);
        mButtonBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck = !isCheck;
                mEXPSwitchView.setCheckedState(isCheck);
            }
        });

    }


}
