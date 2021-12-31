package com.example.wechat.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.Util;
import com.example.wechat.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnItemClickListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.wechat.Utils.MD5Utils.getGlide4_SafeKey;

public class ThoughtImageAdapter extends RecyclerView.Adapter<ThoughtImageAdapter.ViewHolder> {
    public static final String TAG = "ThoughtImageAdapter";
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;
    private LayoutInflater mInflater;
    private List<String> list = new ArrayList<>();
    private int selectMax = 9;
    private List<LocalMedia> localMedia;
    private String cachePath;
    public interface onAddPicClickListener {
        void onAddPicClick();
    }

    public ThoughtImageAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.cachePath=context.getCacheDir().getPath();
    }

    public void setSelectMax(int selectMax) {
        this.selectMax = selectMax;
    }

    public void setList(List<String> list) {
        this.list = list;
        this.localMedia=new ArrayList<>(list.size());
    }

    public List<String> getData() {
        return list == null ? new ArrayList<>() : list;
    }
    public List<LocalMedia> getLocalMedia() {
        return localMedia == null ? new ArrayList<>() : localMedia;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImg;
        public ViewHolder(View view) {
            super(view);
            mImg = view.findViewById(R.id.fiv);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAddItem(position)) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PICTURE;
        }
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ThoughtImageAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.thought_image, viewGroup, false);
        return new ThoughtImageAdapter.ViewHolder(view);
    }

    private boolean isShowAddItem(int position) {
        int size = list.size();
        return position == size;
    }

    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(final ThoughtImageAdapter.ViewHolder viewHolder, final int position) {

        Glide.with(viewHolder.mImg.getContext())
                .load(list.get(position))
                .centerCrop()
                .placeholder(R.color.app_color_f6)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.mImg);

        String cacheFileName=getGlide4_SafeKey(list.get(position));
        //Log.d(TAG,cacheFileName);
        LocalMedia temp=new LocalMedia(cachePath+"/image_manager_disk_cache/"+cacheFileName,0, PictureMimeType.ofImage(),cacheFileName);
        localMedia.add(temp);

        //itemView 的点击事件
        if (mItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(v -> {
                int adapterPosition = viewHolder.getAdapterPosition();
                mItemClickListener.onItemClick(v, adapterPosition);
            });
        }
    }

    private OnItemClickListener mItemClickListener;
    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }


}
