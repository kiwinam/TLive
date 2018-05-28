package com.testexam.charlie.tlive.common.kurento.models.response;

/**
 * Created by charlie on 2018. 5. 28..
 */

public enum TypeResponse {
    ACCEPTED("accepted"),
    REJECTED("rejected");

    private String id;

    TypeResponse(String id){ this.id = id; }

    public static TypeResponse getType(String type){
        for(TypeResponse typeResponse : TypeResponse.values()){
            if(type.equals(typeResponse.getId())){
                return typeResponse;
            }
        }
        return REJECTED;
    }

    public String getId(){ return id; }
}
