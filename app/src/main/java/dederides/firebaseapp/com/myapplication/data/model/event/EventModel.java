package dederides.firebaseapp.com.myapplication.data.model.event;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventModel {

    /* Member Variables ******************************************************/

    private EventModelUpdateHandler m_handler;

    private String m_eventID;

    private DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference m_eventRef;

    private String m_name;
    private String m_location;
    private String m_ownerUID;

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
    }

    /* Accessors *************************************************************/

    public String getEventID() {
        return m_eventID;
    }

    public String getName() {
        return m_name;
    }

    public String getLocation() {
        return m_location;
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
