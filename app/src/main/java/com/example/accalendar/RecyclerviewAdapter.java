package com.example.accalendar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {
    private static final String TAG = "Recyclerview";
    //private ImageView Selection;
    private final ArrayList<Integer> fish;
    //populate this array with the r.drawable files
    private LayoutInflater mInflater;
    private AdapterView.OnItemClickListener clickListener;

    //not done yet, need to figure this part out
    public RecyclerviewAdapter(Context context, ArrayList<Integer> fish){
        this.mInflater = LayoutInflater.from(context);
        this.fish = fish;
        //this.Selection = images;
    }

    //inflates cell layout from xml
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view, parent, false);
        return new ViewHolder(view);
    }

    //binds data to the ImageView to each cell
    //original post used textview, so I need to hammer this part out
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
        //need to setImage???
        holder.myImageView.setImageResource(fish.get(position));
        holder.myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + fish.get(position));
            }
        });

    }

    //total number of cells
    // We have to define getItemCount since it's abstract
    @Override
    public int getItemCount(){
        return fish.size();
    }

    //supposed to store and recycle view as it is scrolled off screen
    //implements ViewHolder stuff so it doesn't show up as an error
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myImageView;

        ViewHolder(View itemView){
            super(itemView);
            myImageView = itemView.findViewById(R.id.fishimg);
            //itemView.setOnClickListener(this);
        }

        /*
        //inner classes cannot have static declaration?
        public interface ItemClickListener {
            void onItemClick(View view, int position);
        }

        //supposed to let click events to be caught, but not really sure how this really works
        @Override
        public void onClick(View view){
            if (clickListener != null){
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

        Integer getItem(int id){
            return fish.get(id);
        }

        void setClickListener(ItemClickListener itemClickListener){
            void onItemClick(View view, int position);
        }*/
    }

}
