package com.example.wechat.Activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.Adapter.SearchAdapter;
import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.customView.ContactListView;
import com.example.wechat.javaBean.ContactBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.wechat.Activity.UserInfoActivity.getConstellation;

public class AddActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private EditText search;

    private RadioButton find_people;

    private final boolean FIND_PEOPLE=true;
    private final boolean FIND_GROUP=false;

    private boolean findMethod=true;

    private RadioButton find_group;
    private List<ContactBean> searchContacts=new ArrayList<>();

    private SearchAdapter searchAdapter;
    private ContactListView searchList;

    public static final int SET_ADAPTER=1;
    public static final int CANT_ADD_YOURSELF=2;
    public static final int IS_YOUR_FRIEND=3;
    public static final int BACK_MESSAGE=4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        radioGroup=findViewById(R.id.add_group);
        search=findViewById(R.id.search_bar);
        find_people=findViewById(R.id.find_people);
        find_group=findViewById(R.id.find_group);

        searchList=(ContactListView)findViewById(R.id.v_list);
        searchAdapter=new SearchAdapter(AddActivity.this,handler);
        searchList.setAdapter(searchAdapter);



        Drawable icon = getResources().getDrawable(R.mipmap.search);
        icon.setBounds(0, 0, 80, 80);

        search.setCompoundDrawables(icon, null, null, null);
        search.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_SEARCH){
                String searchKey=search.getText().toString();
                if(!TextUtils.isEmpty(searchKey)){
                    search.clearFocus();
                    if(findMethod){
                        searchContacts(searchKey);
                    }else{

                    }
                }
            }
            return false;
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.find_people){
                    search.setHint("邮箱/手机号/用户名");
                    findMethod=FIND_PEOPLE;
                    find_people.setTextColor(getResources().getColor(R.color.colorSkyBlue));
                    find_group.setTextColor(getResources().getColor(R.color.app_color_white));
                }else{
                    findMethod=FIND_GROUP;
                    search.setHint("群号");
                    find_people.setTextColor(getResources().getColor(R.color.app_color_white));
                    find_group.setTextColor(getResources().getColor(R.color.colorSkyBlue));
                }
            }
        });


    }

    private void searchContacts(String searchKey){
        searchContacts.clear();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"searchUserInfo.action?" +
                "searchKey="+searchKey).build();//将用户的账号密码传输
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                searchContacts=gson.fromJson(res,new TypeToken<List<ContactBean>>(){}.getType());

                for (int i=0;i<searchContacts.size();i++){
                    ContactBean user=searchContacts.get(i);
                    int age= (int) ((System.currentTimeMillis()-user.getBirthday().getTime())/31536000000L);
                    user.setContact_age(age);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String[] a =simpleDateFormat.format(user.getBirthday()).split("-");
                    user.setContact_constellation(getConstellation(Integer.parseInt(a[1]),Integer.parseInt(a[2])));
                    user.setBirthdayString(a[1]+"-"+a[2]);
                }

                Message message=new Message();
                message.what=SET_ADAPTER;
                handler.sendMessage(message);


            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case SET_ADAPTER:
                searchAdapter.setData(searchContacts);
                break;
            case CANT_ADD_YOURSELF:
                Toast.makeText(getApplicationContext(),"你不能添加自己为好友！",Toast.LENGTH_SHORT).show();
                break;
            case IS_YOUR_FRIEND:
                Toast.makeText(getApplicationContext(),"他已经是你的好友！",Toast.LENGTH_SHORT).show();
                break;
            case BACK_MESSAGE:
                String message=msg.getData().getString("message");
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        }
    };

}
