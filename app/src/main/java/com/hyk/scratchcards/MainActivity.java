package com.hyk.scratchcards;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.hyk.scratchcards.view.ScratchCard;

public class MainActivity extends AppCompatActivity {

    ScratchCard scratchCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scratchCardView = findViewById(R.id.sc_view);
        scratchCardView.setCompleteListener(new ScratchCard.OnScratchCardCompleteListener() {
            @Override
            public void complete() {
                Toast.makeText(MainActivity.this,
                        "刮卡完成", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        scratchCardView.setText("Android 刮刮卡");
    }
}
