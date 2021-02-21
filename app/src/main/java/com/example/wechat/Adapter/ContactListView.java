package com.example.wechat.Adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ContactListView extends ListView {
    public ContactListView(Context context){
        super(context);
    }
    public ContactListView(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    public ContactListView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs,defStyle);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
