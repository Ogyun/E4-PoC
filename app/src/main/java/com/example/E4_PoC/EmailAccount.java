package com.example.E4_PoC;

import android.os.Parcel;
import android.os.Parcelable;

public class EmailAccount implements Parcelable {
    private String emailAddress;
    private String password;
    private String encryptionPassword;

    public EmailAccount(String email, String password){
        this.emailAddress = email;
        this.password = password;
    }
    protected EmailAccount(Parcel in) {
        emailAddress = in.readString();
        password = in.readString();
        encryptionPassword = in.readString();

    }
    public String getPassword(){
        return password;
    }
    public String getEmailAddress(){
        return emailAddress;
    }
    public void setEncryptionPassword(String pwd){
        this.encryptionPassword = pwd;

    }
    public String getEncryptionPassword(){
        return  encryptionPassword;
    }

    public static final Creator<EmailAccount> CREATOR = new Creator<EmailAccount>() {
        @Override
        public EmailAccount createFromParcel(Parcel in) {
            return new EmailAccount(in);
        }

        @Override
        public EmailAccount[] newArray(int size) {
            return new EmailAccount[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(emailAddress);
        dest.writeString(password);
        dest.writeString(encryptionPassword);
    }
}
