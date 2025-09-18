package com.example.scblocationservice.dto;


public class LanDto {
    private final String lanCode;
    private final String lanName;

    public LanDto(String lanCode, String lanName) {
        this.lanCode = lanCode;
        this.lanName = lanName;
    }

    public String getLanCode() {
        return lanCode;
    }

    public String getLanName() {
        return lanName;
    }
}
