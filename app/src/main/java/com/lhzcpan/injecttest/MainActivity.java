package com.lhzcpan.injecttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lhzcpan.annotation.BindView;
import com.lhzcpan.api.InjectHelper;

/**
 * @author master
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_view)
    TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InjectHelper.init(this);

        mTextView.setText("*测试*");

    }
}
