package fi.konstal.bullet_your_life;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by konka on 14.3.2018.
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder> {
        private List<DayCard> cardsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, date, content;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                content = (TextView) view.findViewById(R.id.card_content);
                date = (TextView) view.findViewById(R.id.card_date);
            }
        }


        public CardViewAdapter(List<DayCard> cardsList) {
            this.cardsList = cardsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.day_card, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            DayCard dayCard = cardsList.get(position);
            holder.title.setText(dayCard.getTitle());
            holder.content.setText(dayCard.getContent());
            holder.date.setText(dayCard.getDateString());
        }

        @Override
        public int getItemCount() {
            return cardsList.size();
        }
    }
