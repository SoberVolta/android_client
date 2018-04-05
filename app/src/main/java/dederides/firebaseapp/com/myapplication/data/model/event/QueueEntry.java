package dederides.firebaseapp.com.myapplication.data.model.event;

public class QueueEntry {
    public String rideID;
    public String riderUID;

    public QueueEntry(String rideID, String riderUID) {

        this.rideID = rideID;
        this.riderUID = riderUID;
    }
}
