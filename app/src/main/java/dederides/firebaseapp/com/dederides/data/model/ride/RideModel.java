package dederides.firebaseapp.com.dederides.data.model.ride;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RideModel {

    private static DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();

    private String m_rideID;
    private RideModelUpdateHandler m_handler;

    private DatabaseReference m_rideRef;

    private String m_eventID;
    private String m_riderUID;
    private String m_driverUID;
    private int m_status;
    private Double m_latitude;
    private Double m_longitude;

     /* Constructor **********************************************************/


    public RideModel( String rideID, RideModelUpdateHandler handler ) {

        this.m_rideID = rideID;
        this.m_handler = handler;

        this.m_rideRef = RideModel.m_ref.child( "rides" ).child( this.m_rideID );

        this.m_latitude = null;
        this.m_longitude = null;

        this.m_rideRef.child( "event" ).addValueEventListener(
                new EventListener( this )
        );
        this.m_rideRef.child( "rider" ).addValueEventListener(
                new RiderListener( this )
        );
        this.m_rideRef.child( "driver" ).addValueEventListener(
                new DriverListener( this )
        );
        this.m_rideRef.child( "status" ).addValueEventListener(
                new StatusListener( this )
        );
        this.m_rideRef.child( "latitude" ).addValueEventListener(
                new LatitudeListener( this )
        );
        this.m_rideRef.child( "longitude" ).addValueEventListener(
                new LongitudeListener( this )
        );

    }

    /* Accessors *************************************************************/


    public String getEventID() {
        return m_eventID;
    }

    public String getRiderUID() {
        return m_riderUID;
    }

    public String getDriverUID() {
        return m_driverUID;
    }

    public int getStatus() {
        return m_status;
    }

    public Double getLatitude() {
        return m_latitude;
    }

    public Double getLongitude() {
        return m_longitude;
    }

    /* Database Listeners ****************************************************/

    private class EventListener implements ValueEventListener {

        private RideModel m_model;

        EventListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_eventID = dataSnapshot.getValue( String.class );
            this.m_model.m_handler.rideEventDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class RiderListener implements ValueEventListener {

        private RideModel m_model;

        RiderListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_riderUID = dataSnapshot.getValue( String.class );
            this.m_model.m_handler.rideRiderDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class DriverListener implements ValueEventListener {

        private RideModel m_model;

        DriverListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_driverUID = dataSnapshot.getValue( String.class );
            this.m_model.m_handler.rideDriverDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class StatusListener implements ValueEventListener {

        private RideModel m_model;

        StatusListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_status = dataSnapshot.getValue( Integer.class );
            this.m_model.m_handler.rideStatusDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class LatitudeListener implements ValueEventListener {

        private RideModel m_model;

        LatitudeListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_latitude = dataSnapshot.getValue( Double.class );
            this.m_model.m_handler.rideLocationDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class LongitudeListener implements ValueEventListener {

        private RideModel m_model;

        LongitudeListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_longitude = dataSnapshot.getValue( Double.class );
            this.m_model.m_handler.rideLocationDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }
}
