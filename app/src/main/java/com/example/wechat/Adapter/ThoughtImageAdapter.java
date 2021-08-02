package com.example.wechat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wechat.R;
import com.luck.picture.lib.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ThoughtImageAdapter extends RecyclerView.Adapter<ThoughtImageAdapter.ViewHolder> {
    public static final String TAG = "PictureSelector";
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;
    private LayoutInflater mInflater;
    private List<String> list = new ArrayList<>();
    private int selectMax = 9;


    public interface onAddPicClickListener {
        void onAddPicClick();
    }

    public ThoughtImageAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setSelectMax(int selectMax) {
        this.selectMax = selectMax;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<String> getData() {
        return list == null ? new ArrayList<>() : list;
    }

    public void remove(int position) {
        if (list != null && position < list.size()) {
            list.remove(position);
        }
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



        //itemView 的点击事件
        if (mItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(v -> {

            });
        }


    }

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

}
