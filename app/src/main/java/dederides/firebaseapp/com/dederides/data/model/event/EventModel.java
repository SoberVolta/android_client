package dederides.firebaseapp.com.dederides.data.model.event;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventModel {

    /* Member Variables ******************************************************/

    private EventModelUpdateHandler m_handler;

    private String m_eventID;

    private static DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference m_eventRef;

    private String m_name;
    private String m_location;
    private String m_ownerUID;
    private boolean m_disabled;

    private ArrayList<QueueEntry> m_queue;
    private ArrayList<ActiveRidesEntry> m_activeRides;
    private ArrayList<PendingDriversEntry> m_pendingDrivers;
    private ArrayList<DriversEntry> m_drivers;

    /* Constructor ***********************************************************/

    public EventModel(String eventID, EventModelUpdateHandler handler ) {

        this.m_eventID = eventID;
        this.m_handler = handler;

        this.construct();
    }

    private void construct() {

        /* Initialize database event root */
        this.m_eventRef = m_ref.child( "events" ).child( this.m_eventID );

        /* Initialize Database Populated Values */
        this.m_name = "";
        this.m_location = "";
        this.m_ownerUID = "";
        this.m_disabled = false;
        this.m_queue = new ArrayList<>();
        this.m_activeRides = new ArrayList<>();
        this.m_pendingDrivers = new ArrayList<>();
        this.m_drivers = new ArrayList<>();

        /* Add event Listeners */
        m_eventRef.child( "name" ).addValueEventListener(
                new EventNameValueListener( this )
        );
        m_eventRef.child( "location" ).addValueEventListener(
                new EventLocationValueListener( this )
        );
        m_eventRef.child( "owner" ).addValueEventListener(
                new EventOwnerValueListener( this )
        );
        m_eventRef.child( "disabled" ).addValueEventListener(
                new EventDisabledValueListener( this )
        );
        m_eventRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if( dataSnapshot.getKey().equals( "disabled" ) ) {
                    EventModel.this.m_disabled = false;
                    EventModel.this.m_handler.eventDisabledDidChange();
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        m_eventRef.child( "queue" ).addValueEventListener(
                new EventQueueValueListener( this )
        );
        m_eventRef.child( "activeRides" ).addValueEventListener(
                new ActiveRidesValueListener( this )
        );
        m_eventRef.child( "pendingDrivers" ).addValueEventListener(
                new PendingDriversValueListener( this )
        );
        m_eventRef.child( "drivers" ).addValueEventListener(
                new DriversValueListener( this )
        );
        m_ref.child( "events" ).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if( dataSnapshot.getKey().equals( EventModel.this.getEventID() )) {
                    EventModel.this.m_handler.eventDeleted();
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /* Accessors *************************************************************/

    public static boolean createEvent( String eventName, String eventLocation, String ownerUID ) {

        if( eventName.length() == 0 || eventLocation.length() == 0 || ownerUID.length() == 0 ) {
            return false;
        }

        Map<String, Object> updates = new HashMap<>(2 );
        Map<String, Object> eventData = new HashMap<>( 2 );
        String newEventKey = EventModel.m_ref.child( "events" ).push().getKey();

        eventData.put( "name", eventName );
        eventData.put( "location", eventLocation );
        eventData.put( "owner", ownerUID );

        updates.put( "/events/" + newEventKey, eventData );
        updates.put( "/users/" + ownerUID + "/ownedEvents/" + newEventKey, eventName );

        EventModel.m_ref.updateChildren( updates );

        return true;
    }

    public String getEventID() {
        return m_eventID;
    }

    public String getName() {
        return m_name;
    }

    public String getLocation() {
        return m_location;
    }

    public boolean isDisabled() {
        return m_disabled;
    }

    public String getOwnerUID() {
        return m_ownerUID;
    }

    public ArrayList<QueueEntry> getQueue() {
        return m_queue;
    }

    public ArrayList<ActiveRidesEntry> getActiveRides() {
        return m_activeRides;
    }

    public ArrayList<PendingDriversEntry> getPendingDrivers() {
        return m_pendingDrivers;
    }

    public ArrayList<DriversEntry> getDrivers() {
        return m_drivers;
    }

    public void addDriveOffer(String potentialDriverUID, String potentialDriverDisplayName ) {

        this.m_eventRef.child( "pendingDrivers" ).child( potentialDriverUID ).setValue(
                potentialDriverDisplayName
        );

    }

    public void cancelPendingDriveOffer( String driverUID ) {
        m_eventRef.child( "pendingDrivers" ).child( driverUID ).setValue( null );
    }

    public void removeDriverFromEvent( String driverUID ) {

        Map<String, Object> updates = new HashMap<>( 2 );

        updates.put( "/events/" + this.m_eventID + "/drivers/" + driverUID, null );
        updates.put( "/users/" + driverUID + "/drivesFor/" + this.m_eventID, null );

        EventModel.m_ref.updateChildren( updates );
    }

    public void enqueueNewRideRequest( String riderUID, double lat, double lon ) {

        String rideKey = m_ref.child( "rides" ).push().getKey();
        Map<String, Object> rideData = new HashMap<>();
        Map<String, Object> updates = new HashMap<>();

        rideData.put( "status", 0 );
        rideData.put( "rider", riderUID );
        rideData.put( "event", this.m_eventID );
        rideData.put( "latitude", lat );
        rideData.put( "longitude", lon );

        updates.put( "/rides/" + rideKey, rideData );
        updates.put( "/events/" + this.m_eventID + "/queue/" + rideKey, riderUID );
        updates.put( "/users/" + riderUID + "/rides/" + rideKey, this.m_name );

        EventModel.m_ref.updateChildren( updates );
    }

    public void cancelRideRequest( String riderUID ) {

        CancelRideRequestTransactionHandler transactionHandler
                = new CancelRideRequestTransactionHandler( riderUID );
        this.m_eventRef.child( "queue" ).runTransaction( transactionHandler );

    }

    private class CancelRideRequestTransactionHandler implements Transaction.Handler {

        private String m_cancelingRiderUID;

        CancelRideRequestTransactionHandler( String riderUID ) {
            this.m_cancelingRiderUID = riderUID;
        }

        @Override
        public Transaction.Result doTransaction(MutableData mutableData) {

            /* Local Variables */
            String queuedRiderUID;
            String canceledRideID = null;
            Map<String, Object> updates = new HashMap<>( 2);

            /* For each rider in queue */
            for (MutableData queueEntry: mutableData.getChildren()) {

                /* If the rider in queue is the canceling rider */
                queuedRiderUID = queueEntry.getValue( String.class );
                if ( queuedRiderUID.equals( this.m_cancelingRiderUID ) ) {

                    /* Remove that ride from queue */
                    canceledRideID = queueEntry.getKey();
                    mutableData.child( canceledRideID ).setValue( null );
                    break;
                }
            }

            /* If rider was in queue */
            if ( canceledRideID != null ) {

                /* Update database to remove ride */
                updates.put( "/rides/" + canceledRideID, null );
                updates.put( "/users/" + this.m_cancelingRiderUID + "/rides/" + canceledRideID,
                             null
                );

                FirebaseDatabase.getInstance().getReference().updateChildren( updates );
            }

            return Transaction.success( mutableData );
        }

        @Override
        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

        }
    }

    public void enableEvent() {

        if ( !this.isDisabled() ) {
            return;
        }

        this.m_eventRef.child( "disabled" ).setValue( null );
    }

    public void disableEvent() {

        if ( this.isDisabled() ) {
            return;
        }

        this.m_eventRef.child( "disabled" ).setValue( true );
    }

    public void deleteEvent() {

        this.m_eventRef.runTransaction( new DeleteEventTransactionHandler( this.m_eventID ) );

        EventModel.m_ref.child( "events" ).child( this.m_eventID ).setValue( null );
    }

    private class DeleteEventTransactionHandler implements Transaction.Handler {

        private String m_eventID;

        DeleteEventTransactionHandler( String eventID ) {
            this.m_eventID = eventID;
        }

        @Override
        public Transaction.Result doTransaction(MutableData mutableData) {

            if ( mutableData == null || mutableData.getValue() == null ) {
                return Transaction.success( mutableData );
            }

            Map<String, Object> updates = new HashMap<>();
            MutableData queueData;
            MutableData activeRidesData;
            MutableData activeDriversData;
            String rideID;
            String riderUID;
            String driverUID;
            String ownerUID;

            /* Delete Queue */
            queueData = mutableData.child( "queue" );
            for ( MutableData queueEntry: queueData.getChildren()) {

                /* Get data from queue entry */
                rideID = queueEntry.getKey();
                riderUID = queueEntry.getValue( String.class );

                /* Delete ride */
                updates.put( "/rides/" + rideID, null );

                /* Remove ride from user's space */
                updates.put( "/users/" + riderUID + "/rides/" + rideID, null );
            }

            /* Delete Active Rides */
            activeRidesData = mutableData.child( "activeRides" );
            for (MutableData activeRideEntry : activeRidesData.getChildren()) {

                /* Get data from active ride entry */
                rideID = activeRideEntry.getKey();
                driverUID = activeRideEntry.getValue( String.class );

                /* Remove ride from driver's space */
                updates.put( "/users/" + driverUID + "/drives/" + rideID, null );

                /* Remove ride in rider's space and ride space */
                FirebaseDatabase.getInstance().getReference().child( "rides" ).child( rideID )
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Map<String, Object> updates = new HashMap<>( 2 );
                        String rideID = dataSnapshot.getKey();
                        String riderUID = dataSnapshot.child( "rider" ).getValue( String.class );

                        /* Delete ride from rides space and from rider's rides space */
                        updates.put( "/rides/" + rideID, null );
                        updates.put( "/users/" + riderUID + "/rides/" + rideID, null );

                        FirebaseDatabase.getInstance().getReference().updateChildren( updates );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            /* Delete Active Drivers */
            activeDriversData = mutableData.child( "drivers" );
            for (MutableData activeDriverEntry : activeDriversData.getChildren()) {
                driverUID = activeDriverEntry.getKey();

                /* Remove drivesFor entry in driver's space */
                updates.put( "/users/" + driverUID + "/drivesFor/" + m_eventID, null );
            }

            /* Delete from owner's space */
            ownerUID = mutableData.child( "owner" ).getValue( String.class );
            updates.put( "/users/" + ownerUID + "/ownedEvents/" + m_eventID, null );

            /* Update Other spaces */
            FirebaseDatabase.getInstance().getReference().updateChildren( updates );

            /* Delete this space */
            return Transaction.success( null );
        }

        @Override
        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        }
    }

    public void rejectPendingRideOffer( String driverUID ) {
        this.m_eventRef.child( "pendingDrivers" ).child( driverUID ).setValue( null );
    }

    public void acceptDriveOffer( String driverUID, String driverDisplayName ) {

        Map<String, Object> updates = new HashMap<>( 3 );

        updates.put( "/events/" + this.m_eventID + "/pendingDrivers/" + driverUID, null );
        updates.put( "/events/" + this.m_eventID + "/drivers/" + driverUID, driverDisplayName );
        updates.put( "/users/" + driverUID + "/drivesFor/" + this.m_eventID, this.m_name );

        EventModel.m_ref.updateChildren( updates );
    }

    public void takeNextRideInQueue(final String driverUID ) {

        this.m_eventRef.child( "queue" ).runTransaction(new Transaction.Handler() {

            private String m_rideID;

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                MutableData firstInQueue = mutableData.getChildren().iterator().next();
                this.m_rideID = firstInQueue.getKey();

                /* Remove ride from queue */
                mutableData.child( m_rideID ).setValue( null );

                return Transaction.success( mutableData );
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                Map<String, Object> updates = new HashMap<>( 4 );

                /* Update other spaces */
                updates.put( "/users/" + driverUID + "/drives/" + m_rideID, EventModel.this.m_eventID );
                updates.put( "/rides/" + m_rideID + "/status", 1 );
                updates.put( "/rides/" + m_rideID + "/driver", driverUID );
                updates.put( "/events/" + EventModel.this.m_eventID + "/activeRides/" + m_rideID, driverUID );
                FirebaseDatabase.getInstance().getReference().updateChildren( updates );
            }
        });

    }

    /* Database Value Listeners **********************************************/

    private class EventNameValueListener implements ValueEventListener {

        EventModel m_model;

        EventNameValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            m_model.m_name = dataSnapshot.getValue( String.class );
            m_model.m_handler.eventNameDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class EventLocationValueListener implements ValueEventListener {

        EventModel m_model;

        EventLocationValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            m_model.m_location = dataSnapshot.getValue( String.class );
            m_model.m_handler.eventLocationDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class EventOwnerValueListener implements ValueEventListener {

        EventModel m_model;

        EventOwnerValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            m_model.m_ownerUID = dataSnapshot.getValue( String.class );
            m_model.m_handler.eventOwnerDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class EventDisabledValueListener implements ValueEventListener {

        EventModel m_model;

        EventDisabledValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if( dataSnapshot.getValue() == null ) {
                return;
            }

            this.m_model.m_disabled = dataSnapshot.getValue( Boolean.class );
            this.m_model.m_handler.eventDisabledDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class EventQueueValueListener implements ValueEventListener {

        EventModel m_model;

        EventQueueValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            m_model.m_queue.clear();

            for (DataSnapshot queueEntry : dataSnapshot.getChildren()) {
                this.m_model.m_queue.add( new QueueEntry(
                        queueEntry.getKey(),
                        queueEntry.getValue( String.class )
                ));
            }

            m_model.m_handler.eventQueueDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class ActiveRidesValueListener implements ValueEventListener {

        EventModel m_model;

        ActiveRidesValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            m_model.m_activeRides.clear();

            for (DataSnapshot activeRidesEntry : dataSnapshot.getChildren()) {
                this.m_model.m_activeRides.add( new ActiveRidesEntry(
                        activeRidesEntry.getKey(),
                        activeRidesEntry.getValue( String.class )
                ));
            }

            m_model.m_handler.eventActiveRidesDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class PendingDriversValueListener implements ValueEventListener {

        EventModel m_model;

        PendingDriversValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            m_model.m_pendingDrivers.clear();

            for (DataSnapshot pendingDriversEntry : dataSnapshot.getChildren()) {
                this.m_model.m_pendingDrivers.add( new PendingDriversEntry(
                        pendingDriversEntry.getKey(),
                        pendingDriversEntry.getValue( String.class )
                ));
            }

            m_model.m_handler.eventPendingDriversDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class DriversValueListener implements ValueEventListener {

        EventModel m_model;

        DriversValueListener( EventModel model ) {
            m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            m_model.m_drivers.clear();

            for (DataSnapshot driversEntry : dataSnapshot.getChildren()) {
                this.m_model.m_drivers.add( new DriversEntry(
                        driversEntry.getKey(),
                        driversEntry.getValue( String.class )
                ));
            }

            m_model.m_handler.eventDriversDidChange();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

}
