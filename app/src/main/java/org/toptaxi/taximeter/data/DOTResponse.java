package org.toptaxi.taximeter.data;

public class DOTResponse {
    Integer Code;
    String Body;

    public DOTResponse(Integer code) {
        Code = code;
    }

    public void Set(Integer code, String body){
        Code = code;
        Body = body;
    }

    public Integer getCode() {
        return Code;
    }

    public String getBody() {
        return Body;
    }
}
