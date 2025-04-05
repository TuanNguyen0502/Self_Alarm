package hcmute.edu.vn.selfalarm.smsCall;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.selfalarm.R;

public class BlacklistFragment extends Fragment {
    private RecyclerView blacklistRecyclerView;
    private BlacklistAdapter adapter;
    private List<String> blacklist;
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_blacklist, container, false);

            // Initialize RecyclerView
            blacklistRecyclerView = view.findViewById(R.id.blacklistRecyclerView);
            blacklistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            
            // Initialize blacklist and adapter
            blacklist = new ArrayList<>();
            adapter = new BlacklistAdapter(blacklist, this::removeFromBlacklist);
            blacklistRecyclerView.setAdapter(adapter);

            // Set up FloatingActionButton
            FloatingActionButton fabAddBlacklist = view.findViewById(R.id.fab_add_blacklist);
            if (fabAddBlacklist != null) {
                fabAddBlacklist.setOnClickListener(v -> {
                    if (checkPermissions()) {
                        showAddBlacklistDialog();
                    } else {
                        requestPermissions();
                    }
                });
            }

            // Load initial blacklist
            loadBlacklist();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error initializing blacklist: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private boolean checkPermissions() {
        if (getContext() == null) return false;
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (getActivity() == null) return;
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_PHONE_STATE
                },
                SMS_PERMISSION_REQUEST_CODE);
    }

    private void showAddBlacklistDialog() {
        try {
            if (getContext() == null) return;
            
            // Inflate the dialog layout
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_blacklist, null);
            
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Add to Blacklist")
                    .setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> {
                        android.widget.EditText editText = dialogView.findViewById(R.id.editTextPhoneNumber);
                        if (editText != null) {
                            String phoneNumber = editText.getText().toString().trim();
                            if (!phoneNumber.isEmpty()) {
                                addToBlacklist(phoneNumber);
                            } else {
                                Toast.makeText(getContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addToBlacklist(String phoneNumber) {
        try {
            if (BlacklistService.addToBlacklist(phoneNumber)) {
                blacklist.add(phoneNumber);
                adapter.notifyItemInserted(blacklist.size() - 1);
                Toast.makeText(getContext(), "Added to blacklist", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to add to blacklist", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error adding to blacklist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFromBlacklist(String phoneNumber) {
        try {
            if (BlacklistService.removeFromBlacklist(phoneNumber)) {
                int position = blacklist.indexOf(phoneNumber);
                if (position != -1) {
                    blacklist.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Removed from blacklist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to remove from blacklist", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error removing from blacklist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBlacklist() {
        try {
            blacklist.clear();
            List<String> numbers = BlacklistService.getAllBlacklistedNumbers();
            if (numbers != null) {
                blacklist.addAll(numbers);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading blacklist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                showAddBlacklistDialog();
            } else {
                Toast.makeText(getContext(), "Permissions required to manage blacklist", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 