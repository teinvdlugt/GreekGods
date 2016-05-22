/* Greek Gods: an Android application which shows the family tree of the Greek Gods.
 * Copyright (C) 2016 Tein van der Lugt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.teinvdlugt.android.greekgods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinvdlugt.android.greekgods.models.Person;

import java.util.List;

public class AllPeopleRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int PERSON_ITEM_VIEW_TYPE = 0;
    private static final int NO_PEOPLE_ITEM_VIEW_TYPE = 1;

    private Context context;
    private List<Person> data;
    private Listener listener;

    public AllPeopleRecyclerViewAdapter(Context context, List<Person> data, Listener listener) {
        this.context = context;
        this.data = data;
        this.listener = listener;
    }

    public interface Listener {
        void onClickPerson(Person person);
        void onClickRefreshDatabase();
    }

    public void setData(List<Person> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PERSON_ITEM_VIEW_TYPE:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_all_people, parent, false));
            case NO_PEOPLE_ITEM_VIEW_TYPE:
                return new NoPeopleViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_no_people, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == NO_PEOPLE_ITEM_VIEW_TYPE) return;
        ((ViewHolder) holder).nameTextView.setText(data.get(position).getName());
        ((ViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onClickPerson(data.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (data.isEmpty()) return NO_PEOPLE_ITEM_VIEW_TYPE;
        else return PERSON_ITEM_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        if (data.isEmpty()) return 1;
        if (data == null) return 0;
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.name_textView);
        }
    }

    class NoPeopleViewHolder extends RecyclerView.ViewHolder {
        public NoPeopleViewHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.refresh_database_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onClickRefreshDatabase();
                }
            });
        }
    }
}
