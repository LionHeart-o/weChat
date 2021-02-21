package com.example.wechat.service;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wechat.R;

public class CommonDialog extends AlertDialog {
    private TextView titleTv ;               //显示的标题
    private EditText text;
    private Button positiveBn;  //确认按钮
    public CommonDialog(Context context) {
        super(context);
    }
    private String title;
    private String positive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        initView();     //初始化界面控件
        initEvent();    //初始化界面控件的点击事件
    }
    //初始化界面控件
    private void initView() {
        positiveBn = (Button) findViewById(R.id.positive);
        titleTv = (TextView) findViewById(R.id.title);
        text=(EditText) findViewById(R.id.text);

    }
    //初始化界面控件的显示数据
    private void refreshView() {
        //如果自定义了title和message会 显示自定义的信息，否则不显示title和message的信息
        if (!TextUtils.isEmpty(title)) {
            titleTv.setText(title);                 //设置标题控件的文本为自定义的title
            titleTv.setVisibility(View.VISIBLE); //标题控件设置为显示状态
        }else {
            titleTv.setVisibility(View.GONE);     //标题控件设置为隐藏状态
        }
    }
    //初始化界面的确定和取消监听器
    private void initEvent() {
        //设置确定按钮的点击事件的监听器
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
    }
    @Override
    public void show() {
        super.show();
        refreshView();
    }
    public interface OnClickBottomListener{
        void onPositiveClick();//实现确定按钮点击事件的方法
    }

    public OnClickBottomListener onClickBottomListener;
    public CommonDialog setOnClickBottomListener(OnClickBottomListener
                                                         onClickBottomListener){
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public CommonDialog setTitle(String title) {
        this.title = title;
        return this;
    }
    public CommonDialog setPositive(String positive) {
        this.positive = positive;
        return this;
    }
    public String getText() {
        return text.getText().toString();
    }
}
