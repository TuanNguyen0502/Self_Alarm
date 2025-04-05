package hcmute.edu.vn.selfalarm.smsCall;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.selfalarm.R;

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.BlacklistViewHolder> {
    private final List<String> blacklist;
    private final OnRemoveClickListener onRemoveClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(String phoneNumber);
    }

    public BlacklistAdapter(List<String> blacklist, OnRemoveClickListener listener) {
        this.blacklist = blacklist;
        this.onRemoveClickListener = listener;
    }

    @NonNull
    @Override
    public BlacklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blacklist, parent, false);
        return new BlacklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlacklistViewHolder holder, int position) {
        String phoneNumber = blacklist.get(position);
        holder.phoneNumberText.setText(phoneNumber);
        holder.removeButton.setOnClickListener(v -> onRemoveClickListener.onRemoveClick(phoneNumber));
    }

    @Override
    public int getItemCount() {
        return blacklist.size();
    }

    static class BlacklistViewHolder extends RecyclerView.ViewHolder {
        TextView phoneNumberText;
        ImageButton removeButton;

        BlacklistViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneNumberText = itemView.findViewById(R.id.phoneNumberText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
} 