package mob.godutch.easychat.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String phone;
    private String username;
    private String userId;

    public String getUesrId() {
        return userId;
    }

    public void setUesrId(String uesrId) {
        this.userId = uesrId;
    }

    private Timestamp createdTimestamp;

    public UserModel() {
    }

    public UserModel(String phone, String username, String uesrId, Timestamp createdTimestamp) {
        this.phone = phone;
        this.username = username;
        this.userId = uesrId;
        this.createdTimestamp = createdTimestamp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
