 package hcmute.edu.vn.selfalarm.smsCall.SMS;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

         // Xử lý sự kiện click vào nút xoá
         holder.deleteButton.setOnClickListener(v -> {
             int currentPosition = holder.getAdapterPosition(); // lấy lại vị trí chính xác
             if (currentPosition != RecyclerView.NO_POSITION) {
                 smsList.remove(currentPosition); // xoá khỏi danh sách
                 notifyItemRemoved(currentPosition); // cập nhật giao diện
                 Toast.makeText(v.getContext(), "Đã xoá tin nhắn", Toast.LENGTH_SHORT).show();
             }
         });
     }

     @Override
     public int getItemCount() {
         return smsList.size();
     }

     public static class ViewHolder extends RecyclerView.ViewHolder {
         TextView addressTextView;
         TextView bodyTextView;
         TextView dateTextView;
         ImageButton deleteButton;

         public ViewHolder(@NonNull View itemView) {
             super(itemView);
             addressTextView = itemView.findViewById(R.id.addressTextView);
             bodyTextView = itemView.findViewById(R.id.bodyTextView);
             dateTextView = itemView.findViewById(R.id.dateTextView);
             deleteButton = itemView.findViewById(R.id.button_delete);

         }
     }
 }