package hcmute.edu.vn.selfalarm.smsCall;

public class CallItem {
    private String phoneNumber;
    private String callTime;
    private int callType;

    public CallItem(String phoneNumber, String callTime, int callType) {
        this.phoneNumber = phoneNumber;
        this.callTime = callTime;
        this.callType = callType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }
} 