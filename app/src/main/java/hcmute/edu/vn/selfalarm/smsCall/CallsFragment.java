package hcmute.edu.vn.selfalarm.smsCall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.selfalarm.R;

public class CallsFragment extends Fragment {
    private RecyclerView callsRecyclerView;
    private CallHistoryAdapter adapter;
    private List<CallItem> callList;
    private SimpleDateFormat dateFormat;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;
    private static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 124;
    private String phoneNumberToCall;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        telephonyManager = (TelephonyManager) requireContext().getSystemService(requireContext().TELEPHONY_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calls, container, false);
        
        callsRecyclerView = view.findViewById(R.id.callsRecyclerView);
        callsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        callList = new ArrayList<>();
        adapter = new CallHistoryAdapter();
        callsRecyclerView.setAdapter(adapter);
        
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Set up FloatingActionButton
        FloatingActionButton fabMakeCall = view.findViewById(R.id.fab_make_call);
        fabMakeCall.setOnClickListener(v -> {
            showMakeCallDialog();
        });

        // Load initial calls
        loadCallLog();

        // Set up phone state listener
        setupPhoneStateListener();

        return view;
    }

    private void setupPhoneStateListener() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) 
            == PackageManager.PERMISSION_GRANTED) {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    switch (state) {
                        case TelephonyManager.CALL_STATE_RINGING:
                            // Cuộc gọi đến
                            loadCallLog(); // Cập nhật danh sách cuộc gọi
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            // Đang trong cuộc gọi
                            break;
                        case TelephonyManager.CALL_STATE_IDLE:
                            // Kết thúc cuộc gọi
                            loadCallLog(); // Cập nhật danh sách cuộc gọi
                            break;
                    }
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PHONE_STATE_PERMISSION_REQUEST_CODE);
        }
    }

    private void showMakeCallDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_make_call, null);
        EditText phoneNumberInput = dialogView.findViewById(R.id.phoneNumberInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Make a Call")
                .setView(dialogView)
                .setPositiveButton("Call", (dialog, which) -> {
                    String phoneNumber = phoneNumberInput.getText().toString().trim();
                    if (!phoneNumber.isEmpty()) {
                        phoneNumberToCall = phoneNumber;
                        makePhoneCall(phoneNumber);
                    } else {
                        Snackbar.make(requireView(), "Please enter a phone number", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) 
            == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (phoneNumberToCall != null) {
                    makePhoneCall(phoneNumberToCall);
                }
            } else {
                Snackbar.make(requireView(), "Call permission denied", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == PHONE_STATE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupPhoneStateListener();
            } else {
                Snackbar.make(requireView(), "Phone state permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void loadCallLog() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) 
            == PackageManager.PERMISSION_GRANTED) {
            String[] projection = new String[] {
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
            };

            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = CallLog.Calls.DATE + " DESC";

            try (Cursor cursor = requireContext().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder)) {

                if (cursor != null) {
                    int numberColumn = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int typeColumn = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateColumn = cursor.getColumnIndex(CallLog.Calls.DATE);

                    callList.clear();
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(numberColumn);
                        int type = cursor.getInt(typeColumn);
                        long date = cursor.getLong(dateColumn);
                        String formattedDate = dateFormat.format(new Date(date));

                        CallItem callItem = new CallItem(number, formattedDate, type);
                        callList.add(callItem);
                    }
                    adapter.setCallHistoryItems(callList);
                }
            }
        } else {
            Snackbar.make(requireView(), "Call log permission required", Snackbar.LENGTH_LONG).show();
        }
    }
} 