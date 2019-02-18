package com.example.minhtuyen.mushroomcontroller;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Controller extends AppCompatActivity {
    static final String RESULT_MSG = "resultMsg";

    static Condition curCon;
    EditText editTemp;
    EditText editHumi;
    EditText editLux;
    EditText editR;
    EditText editG;
    EditText editB;
    Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        setTitle("Thay đổi thông số");

        Intent intent = getIntent();
        curCon=(Condition)intent.getSerializableExtra(MainActivity.PUT_STRING) ;

        btnOk = (Button) findViewById(R.id.btnOk);

        editHumi = (EditText) findViewById(R.id.editHumi);
        editTemp = (EditText) findViewById(R.id.editTemp);
        editLux = (EditText) findViewById(R.id.editLux);
        editTemp.setText(String.valueOf(curCon.getTemp()));
        editHumi.setText(String.valueOf(curCon.getHumi()));
        editLux.setText(String.valueOf(curCon.getLux()));
        editR = (EditText) findViewById(R.id.editR);
        editG = (EditText) findViewById(R.id.editG);
        editB = (EditText) findViewById(R.id.editB);
        editR.setText(String.valueOf(curCon.getR()));
        editG.setText(String.valueOf(curCon.getG()));
        editB.setText(String.valueOf(curCon.getB()));

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curCon.setTemp(Float.parseFloat(editTemp.getText().toString()));
                curCon.setLux(Integer.parseInt(editLux.getText().toString()));
                curCon.setHumi(Float.parseFloat(editHumi.getText().toString()));
                curCon.setR(Integer.parseInt(editR.getText().toString()));
                curCon.setG(Integer.parseInt(editG.getText().toString()));
                curCon.setB(Integer.parseInt(editB.getText().toString()));


                Intent resultIntent = new Intent();
                resultIntent.putExtra(MainActivity.GET_STRING, curCon);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    public void onBtnColor(View view) {
        Intent controlActiveIntent = new Intent(Controller.this, ColorActivity.class);
        startActivityForResult(controlActiveIntent, 2);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            editR.setText(String.valueOf(curCon.getR()));
            editG.setText(String.valueOf(curCon.getG()));
            editB.setText(String.valueOf(curCon.getB()));
        }
    }
}
