package hcmute.edu.vn.selfalarm.smsCall.Call;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarm.R;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder> {
    private List<CallItem> callHistoryItems = new ArrayList<>();

    @NonNull
    @Override
    public CallHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_history, parent, false);
        return new CallHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallHistoryViewHolder holder, int position) {
        CallItem item = callHistoryItems.get(position);
        Context context = holder.itemView.getContext();
        
        holder.phoneNumber.setText(item.getPhoneNumber());
        holder.callTime.setText(item.getCallTime());
        
        // Set appropriate icon, text color and description based on call type
        switch (item.getCallType()) {
            case android.provider.CallLog.Calls.INCOMING_TYPE:
                holder.callTypeIcon.setImageResource(R.drawable.ic_call_incoming);
                holder.phoneNumber.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.callTypeText.setText("Cuộc gọi đến");
                holder.callTypeText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case android.provider.CallLog.Calls.OUTGOING_TYPE:
                holder.callTypeIcon.setImageResource(R.drawable.ic_call_outgoing);
                holder.phoneNumber.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                holder.callTypeText.setText("Cuộc gọi đi");
                holder.callTypeText.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case android.provider.CallLog.Calls.MISSED_TYPE:
                holder.callTypeIcon.setImageResource(R.drawable.ic_call_missed);
                holder.phoneNumber.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                holder.callTypeText.setText("Cuộc gọi nhỡ");
                holder.callTypeText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            case android.provider.CallLog.Calls.REJECTED_TYPE:
                holder.callTypeIcon.setImageResource(R.drawable.ic_call_missed);
                holder.phoneNumber.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                holder.callTypeText.setText("Cuộc gọi bị từ chối");
                holder.callTypeText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                holder.callTypeIcon.setImageResource(android.R.drawable.ic_menu_call);
                holder.phoneNumber.setTextColor(context.getResources().getColor(android.R.color.black));
                holder.callTypeText.setText("Cuộc gọi khác");
                holder.callTypeText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return callHistoryItems.size();
    }

    public void setCallHistoryItems(List<CallItem> items) {
        this.callHistoryItems = items;
        notifyDataSetChanged();
    }

    static class CallHistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView callTypeIcon;
        TextView phoneNumber;
        TextView callTypeText;
        TextView callTime;

        CallHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            callTypeIcon = itemView.findViewById(R.id.callTypeIcon);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            callTypeText = itemView.findViewById(R.id.callTypeText);
            callTime = itemView.findViewById(R.id.callTime);
        }
    }
} 