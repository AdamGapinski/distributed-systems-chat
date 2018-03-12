package com.adam;

public class ClientSocketInfo {
    private final String datagramHostAddress;
    private final Integer datagramPortNumber;

    public ClientSocketInfo(String datagramHostAddress, Integer datagramPortNumber) {
        this.datagramHostAddress = datagramHostAddress;
        this.datagramPortNumber = datagramPortNumber;
    }

    public String getDatagramHostAddress() {
        return datagramHostAddress;
    }

    public Integer getDatagramPortNumber() {
        return datagramPortNumber;
    }
}
