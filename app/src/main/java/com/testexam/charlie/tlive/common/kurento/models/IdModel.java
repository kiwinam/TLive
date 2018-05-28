package com.testexam.charlie.tlive.common.kurento.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by charlie on 2018. 5. 28..
 */

public class IdModel implements Serializable {
    @SerializedName("id")
    protected String id;

    public static IdModel create(String id){
        IdModel idModel = new IdModel();
        idModel.setId(id);
        return idModel;
    }

    public String getId(){ return id; }

    public void setId(String id){ this.id = id; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
