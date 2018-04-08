package dederides.firebaseapp.com.dederides.data.model.event;

public class PendingDriversEntry {
    public String driverUID;
    public String driverDisplayName;

    public PendingDriversEntry(String driverUID, String driverDisplayName) {
        this.driverUID = driverUID;
        this.driverDisplayName = driverDisplayName;
    }
}
