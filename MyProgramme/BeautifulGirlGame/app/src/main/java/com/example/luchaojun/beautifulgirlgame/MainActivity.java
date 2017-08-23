package com.example.luchaojun.beautifulgirlgame;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private ImageView viewById;
    private Bitmap copyPicture;
    private Paint paint;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = (ImageView) findViewById(R.id.iv2);
        Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.before);
        copyPicture = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), picture.getConfig());
        paint = new Paint();
        canvas = new Canvas(copyPicture);
        canvas.drawBitmap(picture,new Matrix(), paint);
        viewById.setImageBitmap(copyPicture);
        viewById.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_MOVE:
                        for(int i=-100;i<100;i++){
                            for(int j=-100;j<100;j++){
                                if(Math.sqrt(i*i+j*j) < 100){
                                     copyPicture.setPixel((int)motionEvent.getX()+i,(int)motionEvent.getY()+j, Color.TRANSPARENT);
                                }
                            }
                        }
                        viewById.setImageBitmap(copyPicture);
                        System.out.println("更新iv");
                        break;
                }
                return true;
            }
        });
      }
}
