package com.example.minhtuyen.mushroomcontroller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;


public class ColorActivity extends AppCompatActivity {

    private int r=0;
    private int g=0;
    private int b=0;
    //private RecyclerView recyclerView;
    //private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        //recyclerView=(RecyclerView)findViewById(R.id.recleview);
        //layoutManager=new GridLayoutManager(this,2);
        //recyclerView.setLayoutManager(layoutManager);
    }

    public void btnColorClick(View view) {
        Button btn= (Button)view;
        ColorDrawable buttonBackground = (ColorDrawable)btn.getBackground();
        int intColor=buttonBackground.getColor();
        r=Color.red(intColor);
        g=Color.green(intColor);
        b=Color.blue(intColor);
        Controller.curCon.setR(r);
        Controller.curCon.setG(g);
        Controller.curCon.setB(b);
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
