package co.hanul.hybridapp;

import android.os.Bundle;

import com.btncafe.AsteroidGirl.R;

public class TvActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Settings.mainViewId = R.layout.activity_tv;

        super.onCreate(savedInstanceState);
    }
}
