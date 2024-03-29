package com.example.wechat.Activity.ui.notifications;

import android.os.Bundle;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import com.example.wechat.Adapter.ContactListView;
import com.example.wechat.Adapter.NotificationAdapter;
import com.example.wechat.R;
import com.example.wechat.javaBean.getStaticBean.getNotificationBean;

import java.util.Timer;
import java.util.TimerTask;


public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private ContactListView notification_list;              //列表控件
    private NotificationAdapter adapter;                //列表的适配器

    private int time = 10;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.activity_notifications, container, false);
        init(root);

        return root;
    }
    private void init(View root){

        notification_list= (ContactListView) root.findViewById(R.id.notification_list);
        adapter=new NotificationAdapter(getActivity(),getActivity());
        notification_list.setAdapter(adapter);
        adapter.setData(getNotificationBean.pythonList);

    }

}