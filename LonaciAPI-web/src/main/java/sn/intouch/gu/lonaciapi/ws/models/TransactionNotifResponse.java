package sn.intouch.gu.lonaciapi.ws.models;

public class TransactionNotifResponse {
    private String lonaciTransactionID;
    private String errorCode;
    private String errorMessage;

    public String getLonaciTransactionID() {
        return lonaciTransactionID;
    }

    public void setLonaciTransactionID(String lonaciTransactionID) {
        this.lonaciTransactionID = lonaciTransactionID;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
