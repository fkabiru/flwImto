package com.flwImto.flwImto.models;

public class Meta{
    public Meta(){}
    String sender;
    String sender_country;
    String mobile_number;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender_country() {
        return sender_country;
    }

    public void setSender_country(String sender_country) {
        this.sender_country = sender_country;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }
}