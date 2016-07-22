package com.ryanpotsander.androidcv;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 7/18/16.
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder>{
    OnItemClickListener listener;

    List<PreviewObject> itemList;

    public interface OnItemClickListener {
        void onItemClick(String path, View v);
        Bitmap getLogoImg(); // TODO remove
    }

    public CustomRecyclerViewAdapter(OnItemClickListener listener, List<PreviewObject> data){

        this.listener = listener;

        itemList = data;
    }


    public List<PreviewObject> getItemList(){
        return itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false);

        ViewHolder vh = new ViewHolder(itemView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        PreviewObject item = itemList.get(position);
        holder.itemTextView.setText(item.getLabel());
        holder.preview.setImageBitmap(item.getPreview());

        final String path = item.getPath();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(path, v);
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        View container;

        TextView itemTextView;

        ImageView preview;

        public ViewHolder(View itemView) {
            super(itemView);

            container = itemView;

            itemTextView = (TextView)itemView.findViewById(R.id.item_text_view);

            preview = (ImageView) itemView.findViewById(R.id.preview);
        }
    }
}
