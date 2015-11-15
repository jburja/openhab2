package org.openhab.binding.myq.client.response;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetDeviceAttributeResponse {
    @Expose
    private String attributeValue;
    @Expose
    @SerializedName("UpdatedTime")
    private String updatedTimeInMilli;
    private String updatedTime;

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getUpdatedTimeInMilli() {
        return updatedTimeInMilli;
    }

    public void setUpdatedTimeInMilli(String updatedTimeInMilli) {
        this.updatedTimeInMilli = updatedTimeInMilli;
    }

    public String getUpdatedTime() {
        // TODO: test this time returned
        Date date = new Date(Long.parseLong(updatedTimeInMilli));
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        String dateFormatted = formatter.format(date);
        return (dateFormatted);
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTimeInMilli = updatedTime;
    }
}
