package com.testexam.charlie.tlive.common.kurento.models.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.testexam.charlie.tlive.common.kurento.models.CandidateModel;
import com.testexam.charlie.tlive.common.kurento.models.IdModel;

import java.io.Serializable;

/**
 * Created by charlie on 2018. 5. 28..
 */

public class ServerResponse extends IdModel implements Serializable {
    @SerializedName("response")
    private String response;
    @SerializedName("sdpAnswer")
    private String sdpAnswer;
    @SerializedName("candidate")
    private CandidateModel candidate;
    @SerializedName("message")
    private String message;
    @SerializedName("success")
    private boolean success;
    @SerializedName("from")
    private String from;
    @SerializedName("sender")
    private String sender;

    public IdResponse getIdRes() { return IdResponse.getIdRes(getId()); }

    public TypeResponse getTypeRes() { return TypeResponse.getType(getResponse()); }

    public String getResponse() { return response; }

    public String getSdpAnswer() { return sdpAnswer; }

    public CandidateModel getCandidate() { return candidate; }

    public String getMessage() { return message; }

    public boolean isSuccess() { return success; }

    public String getFrom() { return from; }

    public String getSender() { return sender; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
