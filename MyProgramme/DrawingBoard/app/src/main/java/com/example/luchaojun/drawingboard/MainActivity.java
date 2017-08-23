package com.example.luchaojun.drawingboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import static com.example.luchaojun.drawingboard.R.id.iv;

public class MainActivity extends AppCompatActivity {

    private ImageView viewById;
    private Bitmap board;
    private Bitmap copyBoard;
    private Paint paint;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = (ImageView) findViewById(iv);
        board = BitmapFactory.decodeResource(getResources(), R.drawable.board);
        copyBoard = Bitmap.createBitmap(board.getWidth(), board.getHeight(), board.getConfig());
        paint = new Paint();
        paint.setStrokeWidth(30);
        canvas = new Canvas(copyBoard);
        canvas.drawBitmap(board,new Matrix(), paint);
    }
    public void click(View view){
        viewById.setImageBitmap(copyBoard);
        viewById.setOnTouchListener(new View.OnTouchListener() {

            private int startY;
            private int startX;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        startX = (int)motionEvent.getX();
                        startY = (int)motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int stopX = (int)motionEvent.getX();
                        int stopY = (int)motionEvent.getY();
                        canvas.drawLine(startX, startY,stopX,stopY,paint);
                        viewById.setImageBitmap(copyBoard);
                        startX = stopX;
                        startY = stopY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }
}
