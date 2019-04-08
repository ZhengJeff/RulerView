package com.jeff.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jeff.ruler.RulerData;
import com.jeff.ruler.RulerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RulerView rulerView = findViewById(R.id.ruler_view);
        List<RulerData> mData = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            RulerData<Integer> data = new RulerData<>();
            data.setData(i);
            data.setText("测试" + (i + 1));
            mData.add(data);
        }
        rulerView.setData(mData);
        rulerView.setOnRulerChangeListener(new RulerView.OnRulerChangeListener() {
            @Override
            public void onRulerChange(int position, RulerData curData) {
                Log.e("zjs",curData.getData().toString());
            }
        });
    }
}
