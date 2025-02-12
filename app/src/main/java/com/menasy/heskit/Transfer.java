package com.menasy.heskit;

public class Transfer
{
    private String transferDate;
    private String sentToPerson;
    private long amountTransfer;
    private int id;

    public Transfer(long amountTransfer, String transferDate, String sentToPerson)
    {
        this.amountTransfer = amountTransfer;
        this.transferDate = transferDate;
        this.sentToPerson = sentToPerson;
    }

    public String getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(String transferDate) {
        this.transferDate = transferDate;
    }

    public String getSentToPerson() {
        return sentToPerson;
    }

    public void setSentToPerson(String sentToPerson) {
        this.sentToPerson = sentToPerson;
    }

    public long getAmountTransfer() {
        return amountTransfer;
    }

    public void setAmountTransfer(long amountTransfer) {
        this.amountTransfer = amountTransfer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
