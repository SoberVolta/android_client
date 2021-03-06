package dederides.firebaseapp.com.dederides.data.model.ride;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

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
        m_ref.child( "rides" ).addChildEventListener( new RideRemovedListener() );
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

    public void endActiveRide() {

        Map<String, Object> updates = new HashMap<>( 4 );

        updates.put( "/rides/" + this.m_rideID, null );
        updates.put( "/events/" + this.m_eventID + "/activeRides/" + this.m_rideID, null );
        updates.put( "/users/" + this.m_driverUID + "/drives/" + this.m_rideID, null );
        updates.put( "/users/" + this.m_riderUID + "/rides/" + this.m_rideID, null );

        RideModel.m_ref.updateChildren( updates );
    }

    public void cancelRideRequest() {

        CancelRideRequestTransactionHandler transactionHandler
                = new CancelRideRequestTransactionHandler( this.m_riderUID );
        m_ref.child( "events" ).child( this.m_eventID ).child( "queue" ).runTransaction(
            transactionHandler
        );

    }

    private class CancelRideRequestTransactionHandler implements Transaction.Handler {

        private String m_cancelingRiderUID;
        String m_canceledRideID;

        CancelRideRequestTransactionHandler( String riderUID ) {
            this.m_cancelingRiderUID = riderUID;
        }

        @Override
        public Transaction.Result doTransaction(MutableData mutableData) {

            /* Local Variables */
            String queuedRiderUID;

            m_canceledRideID = null;

            /* For each rider in queue */
            for (MutableData queueEntry: mutableData.getChildren()) {

                /* If the rider in queue is the canceling rider */
                queuedRiderUID = queueEntry.getValue( String.class );
                if ( queuedRiderUID.equals( this.m_cancelingRiderUID ) ) {

                    /* Remove that ride from queue */
                    m_canceledRideID = queueEntry.getKey();
                    mutableData.child( m_canceledRideID ).setValue( null );
                    break;
                }
            }

            return Transaction.success( mutableData );
        }

        @Override
        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            Map<String, Object> updates;

            if ( m_canceledRideID != null ) {

                updates = new HashMap<>( 2);

                /* Update database to remove ride */
                updates.put( "/rides/" + m_canceledRideID, null );
                updates.put( "/users/" + this.m_cancelingRiderUID + "/rides/" + m_canceledRideID,
                        null
                );

                FirebaseDatabase.getInstance().getReference().updateChildren( updates );
            }
        }
    }

    /* Database Listeners ****************************************************/

    private class RideRemovedListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if( dataSnapshot.getKey().equals( RideModel.this.m_rideID )) {
                RideModel.this.m_handler.rideWasRemoved();
            }
        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }

    private class EventListener implements ValueEventListener {

        private RideModel m_model;

        EventListener( RideModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {
                this.m_model.m_eventID = dataSnapshot.getValue( String.class );
            } catch ( Exception e ) {
                this.m_model.m_eventID = null;
            }
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
            try {
                this.m_model.m_riderUID = dataSnapshot.getValue( String.class );
            } catch ( Exception e ) {
                this.m_model.m_riderUID = null;
            }
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
            try {
                this.m_model.m_driverUID = dataSnapshot.getValue( String.class );
            } catch ( Exception e ) {
                this.m_model.m_driverUID = null;
            }
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
            try {
                this.m_model.m_status = dataSnapshot.getValue(Integer.class);
            } catch ( Exception e ) {
                this.m_model.m_status = 0;
            }
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
            try {
                this.m_model.m_latitude = dataSnapshot.getValue( Double.class );
            } catch ( Exception e ) {
                this.m_model.m_latitude = null;
            }
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
            try {
                this.m_model.m_longitude = dataSnapshot.getValue( Double.class );
            } catch ( Exception e ) {
                this.m_model.m_longitude = null;
            }
            this.m_model.m_handler.rideLocationDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }
}
