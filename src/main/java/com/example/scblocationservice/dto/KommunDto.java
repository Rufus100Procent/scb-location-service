package com.example.scblocationservice.dto;


public class KommunDto {
    private final String kommunCode;
    private final String kommunName;
    private final String lanCode;
    private final String lanName;

    public KommunDto(String kommunCode, String kommunName, String lanCode, String lanName) {
        this.kommunCode = kommunCode;
        this.kommunName = kommunName;
        this.lanCode = lanCode;
        this.lanName = lanName;
    }

    public String getKommunCode() {
        return kommunCode;
    }

    public String getKommunName() {
        return kommunName;
    }

    public String getLanCode() {
        return lanCode;
    }

    public String getLanName() {
        return lanName;
    }
}

