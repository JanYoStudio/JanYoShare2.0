package pw.janyo.janyoshare.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.util.wifi.APUtil;
import vip.mystery0.tools.logs.Logs;

public class FaceToFaceActivity extends AppCompatActivity {
    private static final String TAG = "FaceToFaceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_to_face);

        FloatingActionButton fab_receive = findViewById(R.id.floatingActionButtonReceive);
        FloatingActionButton fab_send = findViewById(R.id.floatingActionButtonSend);

        fab_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i(TAG, "onClick: receive");
            }
        });
        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i(TAG, "onClick: send");
            }
        });
    }

    private void ready(){
//        APUtil.openAP()
    }
}
