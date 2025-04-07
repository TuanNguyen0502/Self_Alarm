package hcmute.edu.vn.selfalarm.smsCall.SMS;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarm.R;

public class MessagesFragment extends Fragment implements SMSUpdateListener {
    private RecyclerView messagesRecyclerView;
    private SMSAdapter adapter;
    private List<SmsItem> messageList;
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize RecyclerView
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize message list and adapter
        messageList = new ArrayList<>();
        adapter = new SMSAdapter(messageList);
        messagesRecyclerView.setAdapter(adapter);

        // Set up FloatingActionButton
        FloatingActionButton fabSendSMS = view.findViewById(R.id.fab_send_sms);
        fabSendSMS.setOnClickListener(v -> {
            if (checkSMSPermission()) {
                openSendSMSActivity();
            } else {
                requestSMSPermission();
            }
        });

        // Load initial messages
        loadMessages();

        return view;
    }

    private boolean checkSMSPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE);
    }

    private void openSendSMSActivity() {
        Intent intent = new Intent(getActivity(), SendSMSActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register SMS update listener
        SMSManager.getInstance().registerListener(this);
        // Reload messages when fragment is visible
        loadMessages();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister SMS update listener
        SMSManager.getInstance().unregisterListener(this);
    }

    private void loadMessages() {
        // Clear existing messages
        messageList.clear();
        
        // Load messages from SMSManager
        List<SmsItem> messages = SMSManager.getInstance().getMessages();
        messageList.addAll(messages);
        
        // Notify adapter of changes
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSMSReceived() {
        // Update UI on main thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadMessages);
        }
    }
} 