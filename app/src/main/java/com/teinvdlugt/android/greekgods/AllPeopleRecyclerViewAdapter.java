package com.teinvdlugt.android.greekgods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.List;

public class AllPeopleRecyclerViewAdapter extends RecyclerView.Adapter<AllPeopleRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<Person> data;
    private OnPersonClickListener onPersonClickListener;

    public AllPeopleRecyclerViewAdapter(Context context, List<Person> data, OnPersonClickListener onPersonClickListener) {
        this.context = context;
        this.data = data;
        this.onPersonClickListener = onPersonClickListener;
    }

    public interface OnPersonClickListener {
        void onClickPerson(Person person);
    }

    public void setData(List<Person> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_all_people, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.nameTextView.setText(data.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPersonClickListener != null)
                    onPersonClickListener.onClickPerson(data.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.name_textView);
        }
    }
}
