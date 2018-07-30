package mkruglikov.bestcafe.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import mkruglikov.bestcafe.R;

public class BookingPeopleAdapter extends RecyclerView.Adapter<BookingPeopleAdapter.ViewHolder> {

    private int maxPeopleCount, selectedPeopleCount;
    private List<PeopleButton> peopleButtons;
    private OnPeopleButtonClickListener onPeopleButtonClickListener;
    private Context context;

    public BookingPeopleAdapter(Context context, int maxPeopleCount, int selectedPeopleCount, OnPeopleButtonClickListener onPeopleButtonClickListener) {
        this.context = context;
        this.maxPeopleCount = maxPeopleCount;
        this.selectedPeopleCount = selectedPeopleCount;
        this.onPeopleButtonClickListener = onPeopleButtonClickListener;
        peopleButtons = new ArrayList<>();
        for (int i = 1; i <= maxPeopleCount; i++) {
            peopleButtons.add(new PeopleButton(i));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final Button btnItemBookingPeople;

        ViewHolder(View itemView) {
            super(itemView);
            btnItemBookingPeople = itemView.findViewById(R.id.btnItemBookingPeople);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_people, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (selectedPeopleCount == position + 1) {
            holder.btnItemBookingPeople.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_red));
            holder.btnItemBookingPeople.setTextColor(Color.WHITE);
        }
        holder.btnItemBookingPeople.setText(String.valueOf(position + 1));
        holder.btnItemBookingPeople.setOnClickListener(view -> {
            holder.btnItemBookingPeople.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_red));
            holder.btnItemBookingPeople.setTextColor(Color.WHITE);
            onPeopleButtonClickListener.onButtonClicked(position + 1);
        });
    }

    @Override
    public int getItemCount() {
        return maxPeopleCount;
    }

    public interface OnPeopleButtonClickListener {
        void onButtonClicked(int noOfPeople);
    }

    class PeopleButton {
        int id;

        public int getId() {
            return id;
        }

        public PeopleButton(int id) {
            this.id = id;
        }
    }

}
