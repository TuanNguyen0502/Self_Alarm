package hcmute.edu.vn.selfalarm.smsCall.SMS;

public class SmsItem {
    private String address;
    private String body;
    private long date;

    public SmsItem(String address, String body, long date) {
        this.address = address;
        this.body = body;
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }
} 