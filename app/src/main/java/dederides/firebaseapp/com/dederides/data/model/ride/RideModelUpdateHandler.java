package dederides.firebaseapp.com.dederides.data.model.ride;

public interface RideModelUpdateHandler {
    void rideEventDidChange();
    void rideRiderDidChange();
    void rideDriverDidChange();
    void rideStatusDidChange();
    void rideLocationDidChange();
    void rideWasRemoved();
}
