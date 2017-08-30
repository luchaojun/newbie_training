package com.example.luchaojun.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


//此段代码只能去实现两个数加减乘除的操作 不使用三个数及以上的操作
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static TextView data;
    private String str = "";

    //定义各个按钮对象的实例
    private static Button one_btn,two_btn,three_btn,jia_btn,four_btn,five_btn,six_btn,jian_btn,seven_btn,eight_btn,nine_btn,cheng_btn,dian_btn,zero_btn,chu_btn,deng_btn,quxiao_btn;
    private static List<String> list = new ArrayList<>();
    private Button qingkong_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = (TextView) findViewById(R.id.data_tv); //显示框的实例

        //所有button按钮的实例
        one_btn = (Button) findViewById(R.id.one_btn);
        two_btn = (Button) findViewById(R.id.two_btn);
        three_btn = (Button) findViewById(R.id.three_btn);
        jia_btn = (Button) findViewById(R.id.jia_btn);
        four_btn = (Button) findViewById(R.id.four_btn);
        five_btn = (Button) findViewById(R.id.five_btn);
        six_btn = (Button) findViewById(R.id.six_btn);
        jian_btn = (Button) findViewById(R.id.jian_btn);
        seven_btn = (Button) findViewById(R.id.seven_btn);
        eight_btn = (Button) findViewById(R.id.eight_btn);
        nine_btn = (Button) findViewById(R.id.nine_btn);
        cheng_btn = (Button) findViewById(R.id.cheng_btn);
        dian_btn = (Button) findViewById(R.id.dian_btn);
        zero_btn = (Button) findViewById(R.id.zero_btn);
        chu_btn = (Button) findViewById(R.id.chu_btn);
        deng_btn = (Button) findViewById(R.id.deng_btn);
        qingkong_btn = (Button) findViewById(R.id.qingkong_btn);

        //给所有的按钮设置点击的事件
        one_btn.setOnClickListener(this);
        two_btn.setOnClickListener(this);
        three_btn.setOnClickListener(this);
        jia_btn.setOnClickListener(this);
        four_btn.setOnClickListener(this);
        five_btn.setOnClickListener(this);
        six_btn.setOnClickListener(this);
        jian_btn.setOnClickListener(this);
        seven_btn.setOnClickListener(this);
        eight_btn.setOnClickListener(this);
        nine_btn.setOnClickListener(this);
        cheng_btn.setOnClickListener(this);
        dian_btn.setOnClickListener(this);
        zero_btn.setOnClickListener(this);
        chu_btn.setOnClickListener(this);
        deng_btn.setOnClickListener(this);
        qingkong_btn.setOnClickListener(this);

        setButtonState(false);  //锁死加减乘除按钮
        deng_btn.setEnabled(false);
    }

    //定义所有按钮的onclick事件
    @Override
    public void onClick(View view) {
        int id = view.getId(); //获取哪个按钮点击的id
        switch (id){
            //按数字0的时候
            case R.id.zero_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"0");
                break;

            //按数字1的时候
            case R.id.one_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"1");
                break;

            //按数字2的时候
            case R.id.two_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"2");
                break;

            //按数字3的时候
            case R.id.three_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"3");
                break;

            //按数字4的时候
            case R.id.four_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"4");
                break;

            //按数字5的时候
            case R.id.five_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"5");
                break;

            //按数字6的时候
            case R.id.six_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"6");
                break;

            //按数字7的时候
            case R.id.seven_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"7");
                break;

            //按数字8的时候
            case R.id.eight_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"8");
                break;

            //按数字9的时候
            case R.id.nine_btn:
                setButtonState(true); //解锁加减乘除按钮
                deng_btn.setEnabled(true);
                data.setText(data.getText().toString()+"9");
                break;

            //按+的时候
            case R.id.jia_btn:
                setButtonState(false);
                deng_btn.setEnabled(false);
                dian_btn.setEnabled(true);
                String string = data.getText().toString();
                String string2 = string.substring(str.length(),string.length());
                str = string+"+";
                list.add(string2);
                list.add("+");
                data.setText(str);
                break;

            //按-的时候
            case R.id.jian_btn:
                dian_btn.setEnabled(true);
                setButtonState(false);
                deng_btn.setEnabled(false);
                String string3 = data.getText().toString();
                String string4 = string3.substring(str.length(),string3.length());
                str = string3+"-";
                list.add(string4);
                list.add("-");
                data.setText(str);
                break;

            //按×的时候
            case R.id.cheng_btn:
                setButtonState(false);
                dian_btn.setEnabled(true);
                deng_btn.setEnabled(false);
                String string5 = data.getText().toString();
                String string6 = string5.substring(str.length(),string5.length());
                str = string5+"×";
                list.add(string6);
                list.add("×");
                data.setText(str);
                break;

            //按/的时候
            case R.id.chu_btn:
                setButtonState(false);
                dian_btn.setEnabled(true);
                deng_btn.setEnabled(false);
                String string7 = data.getText().toString();
                String string8 = string7.substring(str.length(),string7.length());
                str = string7+"/";
                list.add(string8);
                list.add("/");
                data.setText(str);
                break;

            //按点的时候
            case R.id.dian_btn:
                dian_btn.setEnabled(false);
                data.setText(data.getText().toString()+".");
                break;

            //按=的时候
            case R.id.deng_btn:
                deng_btn.setEnabled(false);
                String string9 = data.getText().toString();
                String string10 = string9.substring(str.length(),string9.length());
                str = string9+"/";
                list.add(string10);
                iteratorChengChuMethod(list);
                iteratorJiaJianMethod(list);
                System.out.println(list.size());
                String result = list.get(0);
                String[] split = result.split("[.]");
                if(split.length>1 && "0".equals(split[1])){
                    data.setText(split[0]);
                }else{
                    data.setText(result);
                }
                list.clear();
                str = "";
                break;

            //按CL的时候
            case R.id.qingkong_btn:
                data.setText("");
                setButtonState(false);
                deng_btn.setEnabled(false);
                dian_btn.setEnabled(true);
                str = "";
                break;
        }
    }

    //先计算乘除的计算的递归代码
    public static void iteratorChengChuMethod(List<String> list){
        for(String str : list){
            double a = 0;
            if("/".equals(str) || "×".equals(str)){
                int index = list.indexOf(str);  //拿到索引
                double chuA = Double.parseDouble(list.get(index-1));
                double chuB = Double.parseDouble(list.get(index+1));
                if("/".equals(str)){
                    a = chuA / chuB;
                }
                if("×".equals(str)){
                    a = chuA * chuB;
                }
                list.set(index-1, ""+a);  //给index重新设置值
                list.remove(index);
                list.remove(index);
                iteratorChengChuMethod(list);  //递归调用
                break;
            }
        }
    }


    //再计算加减的计算的递归代码
    public static void iteratorJiaJianMethod(List<String> list){
        for(String str : list){
            double a = 0;
            if("+".equals(str) || "-".equals(str)){
                int index = list.indexOf(str);  //拿到索引
                double chuA = Double.parseDouble(list.get(index-1));
                double chuB = Double.parseDouble(list.get(index+1));
                if("+".equals(str)){
                    a = chuA + chuB;
                }
                if("-".equals(str)){
                    a = chuA - chuB;
                }
                list.set(index-1, ""+a);   //给index重新设置值
                list.remove(index);
                list.remove(index);
                iteratorJiaJianMethod(list);  //递归调用
                break;
            }
        }
    }

    //控制加减乘除的按键状态
    public static void setButtonState(boolean b){
        jia_btn.setEnabled(b);
        jian_btn.setEnabled(b);
        cheng_btn.setEnabled(b);
        chu_btn.setEnabled(b);
    }

}
