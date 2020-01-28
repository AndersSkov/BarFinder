package com.example.barfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BarListAdapter extends RecyclerView.Adapter<BarListAdapter.Holder> {

    List<ModelBar> bars;
    Context context;

    public BarListAdapter(List<ModelBar> bars, Context context) {
        this.bars = bars;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bar_list_card, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final String name = bars.get(position).getName();
        final String address = bars.get(position).getAddress();

        holder.NameOfBar.setText(name);
        holder.AddressOfBar.setText(address);
    }

    @Override
    public int getItemCount() {
        return bars.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        TextView NameOfBar, AddressOfBar;
        public Holder(@NonNull View itemView) {
            super(itemView);

            NameOfBar = itemView.findViewById(R.id.NameOfBarCard);
            AddressOfBar = itemView.findViewById(R.id.AddressOfBarCard);
        }
    }

}
