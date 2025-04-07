package hcmute.edu.vn.selfalarm.smsCall;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import hcmute.edu.vn.selfalarm.smsCall.SMS.MessagesFragment;
import hcmute.edu.vn.selfalarm.smsCall.Call.CallsFragment;
import hcmute.edu.vn.selfalarm.smsCall.Call.Blacklist.BlacklistFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CallsFragment();
            case 1:
                return new MessagesFragment();
            case 2:
                return new BlacklistFragment();
            default:
                return new CallsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Calls, Messages, Blacklist
    }
} 