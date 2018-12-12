package com.example.shinelon.qrcodelogo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorPicActivity extends AppCompatActivity {
    @BindView(R.id.color_pane)
    ImageView pane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pic);
        ButterKnife.bind(this);
        pane.setDrawingCacheEnabled(true);
        pane.setOnTouchListener(new View.OnTouchListener(){

            @Override

            public boolean onTouch(View view, MotionEvent event) {

                int x = (int) event.getX();



                int y = (int) event.getY();



                Bitmap bitmap = pane.getDrawingCache();



                if (event.getAction() == MotionEvent.ACTION_UP) {

                    int color = bitmap.getPixel(x, y);

                    int r = Color.red(color);

                    int g = Color.green(color);

                    int b = Color.blue(color);

                    int a = Color.alpha(color);

                    Intent intent = getIntent();

                    intent.putExtra("r",r);
                    intent.putExtra("g",g);
                    intent.putExtra("b",b);
                    intent.putExtra("a",a);
                    setResult(101,intent);
                    finish();

                    Log.e("rgb", "r=" + r + ",g=" + g + ",b=" + b+",a="+a);

                }

                return true;

            }

        });
    }
}
