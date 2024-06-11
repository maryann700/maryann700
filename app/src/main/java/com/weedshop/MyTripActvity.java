package com.weedshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weedshop.Adapter.MyTripAdapter;
import com.weedshop.Adapter.OrderHistoryAdapter;

public class MyTripActvity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView rvTrip;
    TextView txtBack;
    private MyTripAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip_actvity);
        txtBack = findViewById(R.id.txtBack);
        rvTrip = findViewById(R.id.rv_trip);
        setAdapter();
        setOnClick();
    }

    private void setAdapter() {
        layoutManager = new LinearLayoutManager(this);
        rvTrip.setLayoutManager(layoutManager);
        //rvTrip.setItemAnimator(new DefaultItemAnimator());
        adapter = new MyTripAdapter(MyTripActvity.this);
        rvTrip.setAdapter(adapter);
    }

    private void setOnClick() {
        txtBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtBack:
                finish();
                break;
        }
    }
}