 package hcmute.edu.vn.selfalarm.smsCall;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.selfalarm.R;

 public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.ViewHolder> {
     private List<SmsItem> smsList;

     public SMSAdapter(List<SmsItem> smsList) {
         this.smsList = smsList;
     }

     @NonNull
     @Override
     public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.item_sms, parent, false);
         return new ViewHolder(view);
     }

     @Override
     public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         SmsItem sms = smsList.get(position);
         holder.addressTextView.setText(sms.getAddress());
         holder.bodyTextView.setText(sms.getBody());

         SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
         String dateString = sdf.format(new Date(sms.getDate()));
         holder.dateTextView.setText(dateString);
     }

     @Override
     public int getItemCount() {
         return smsList.size();
     }

     public static class ViewHolder extends RecyclerView.ViewHolder {
         TextView addressTextView;
         TextView bodyTextView;
         TextView dateTextView;

         public ViewHolder(@NonNull View itemView) {
             super(itemView);
             addressTextView = itemView.findViewById(R.id.addressTextView);
             bodyTextView = itemView.findViewById(R.id.bodyTextView);
             dateTextView = itemView.findViewById(R.id.dateTextView);
         }
     }
 }