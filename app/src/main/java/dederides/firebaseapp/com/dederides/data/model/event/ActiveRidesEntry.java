package dederides.firebaseapp.com.dederides.data.model.event;

public class ActiveRidesEntry {
    public String rideID;
    public String driverUID;

    public ActiveRidesEntry(String rideID, String driverUID) {
        this.rideID = rideID;
        this.driverUID = driverUID;
    }
}
