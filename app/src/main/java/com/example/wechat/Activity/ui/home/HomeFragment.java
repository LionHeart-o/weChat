package com.example.wechat.Activity.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.wechat.Activity.AddActivity;
import com.example.wechat.Activity.CreateGroupChatActivity;
import com.example.wechat.Adapter.ConversationAdapter;
import com.example.wechat.customView.ContactListView;
import com.example.wechat.R;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.ConversationBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.Utils.WsManager;


import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ContactListView slv_list;              //列表控件
    private ConversationAdapter adapter;                //列表的适配器
    private ImageView action_list;

    private List<ConversationBean> conversationBeanList;
    private WsManager wsManager=WsManager.getInstance();
    private PopupWindow popupWindow;

    private LinearLayout add_friend_or_group;
    private LinearLayout generate_group;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.activity_contacts, container, false);
        conversationBeanList=LoginBean.getInstance().getConversations();

        init(root);

        return root;
    }
    private void init(View root){


        slv_list= (ContactListView) root.findViewById(R.id.v_list);
        action_list=(ImageView) root.findViewById(R.id.action_list);


        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_window, null);
        add_friend_or_group=contentView.findViewById(R.id.add_friend_or_group);
        generate_group=contentView.findViewById(R.id.generate_group);

        popupWindow = new PopupWindow(getContext());
        popupWindow.setContentView(contentView);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setOutsideTouchable(true);
        action_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
                else {
                    popupWindow.showAsDropDown(view);
                }
            }
        });

        add_friend_or_group.setOnClickListener(v -> {
            Intent intent=new Intent(getActivity(), AddActivity.class);
            startActivity(intent);
        });

        generate_group.setOnClickListener(v -> {
            Intent intent=new Intent(getActivity(), CreateGroupChatActivity.class);
            startActivity(intent);
        });

        adapter=new ConversationAdapter(getActivity());
        slv_list.setAdapter(adapter);
        adapter.setData(conversationBeanList);

        if(wsManager.getConversationAdapter()==null){
            wsManager.setConversationAdapter(adapter);
        }

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }



}