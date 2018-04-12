package dederides.firebaseapp.com.dederides.data.model.user;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class UserModel {

    private static final String TAG = "UserModel";

    private UserModelUpdateHandler m_handler;

    private FirebaseUser m_firebaseUser;
    private String m_displayName;
    private String m_uid;

    private static DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference m_userRef;

    private ArrayList<OwnedEventEntry> m_ownedEvents;
    private ArrayList<SavedEventEntry> m_savedEvents;
    private ArrayList<DrivesForEntry> m_drivesFor;
    private ArrayList<RidesEntry> m_rides;
    private ArrayList<DrivesEntry> m_drives;

    private ArrayList<String> m_notificationSubscriptions;

    public UserModel( FirebaseUser user, UserModelUpdateHandler handler ) {

        /* Initial model variables */
        this.m_handler = handler;

        /* Initialize auth variables */
        this.m_displayName = user.getDisplayName();
        this.m_uid = user.getUid();

         /* Initialize database user root */
        this.m_userRef = m_ref.child( "users" ).child( this.m_uid );

        /* Add user to database if not present */
        this.m_firebaseUser = user;
        CreateUserInDatabaseIfAbsent createUser = new CreateUserInDatabaseIfAbsent( this );
        m_userRef.child( "displayName" ).addListenerForSingleValueEvent( createUser );

        this.construct();
    }

    public UserModel( String user_uid, UserModelUpdateHandler handler ) {

        /* Initial model variables */
        this.m_handler = handler;

        /* Initialize auth variables */
        this.m_uid = user_uid;

        /* Initialize database user root */
        this.m_userRef = m_ref.child( "users" ).child( this.m_uid );

        this.construct();
    }

    private void construct() {

        /* Initialize Database Populated Variables */
        this.m_ownedEvents = new ArrayList<>();
        this.m_savedEvents = new ArrayList<>();
        this.m_drivesFor = new ArrayList<>();
        this.m_rides = new ArrayList<>();
        this.m_drives = new ArrayList<>();

        this.m_notificationSubscriptions = new ArrayList<>();

        /* Add Event Listeners */
        m_userRef.child( "displayName" ).addValueEventListener(
                new DisplayNameListener()
        );
        m_userRef.child( "ownedEvents" ).addValueEventListener(
                new OwnedEventListener( this )
        );
        m_userRef.child( "savedEvents" ).addValueEventListener(
                new SavedEventListener( this )
        );
        m_userRef.child( "drivesFor" ).addValueEventListener(
                new DrivesForListener( this )
        );
        m_userRef.child( "rides" ).addValueEventListener(
                new RidesEventListener( this )
        );
        m_userRef.child( "drives" ).addValueEventListener(
                new DrivesEventListener( this )
        );
    }

    private void subscribeToNotifications(String eventID ) {

        Log.d( TAG, "Subscribing to events for: " + eventID );

        this.m_notificationSubscriptions.add( eventID );
        FirebaseMessaging.getInstance().subscribeToTopic( eventID );
    }

    private void unsubscribeFromAllNotifications() {
        for (String notificationTopic : this.m_notificationSubscriptions) {
            Log.d( TAG, "Unsubscribing from " + notificationTopic + " notifications." );
            FirebaseMessaging.getInstance().unsubscribeFromTopic( notificationTopic );
        }
    }

    public String getDisplayName() {
        return m_displayName;
    }

    public String getUID() {
        return m_uid;
    }

    public ArrayList<OwnedEventEntry> getOwnedEvents() {
        return m_ownedEvents;
    }

    public ArrayList<SavedEventEntry> getSavedEvents() {
        return m_savedEvents;
    }

    public ArrayList<DrivesForEntry> getDrivesFor() {
        return m_drivesFor;
    }

    private UserModelUpdateHandler getHandler() {
        return this.m_handler;
    }

    public ArrayList<RidesEntry> getRides() {
        return m_rides;
    }

    public ArrayList<DrivesEntry> getDrives() {
        return m_drives;
    }

    private class CreateUserInDatabaseIfAbsent implements ValueEventListener {

        private UserModel m_model;

        CreateUserInDatabaseIfAbsent( UserModel model ) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            String userDisplayNameValue = dataSnapshot.getValue( String.class );

            if( userDisplayNameValue == null ) {

                UserModel.m_ref.child( "users" ).child( this.m_model.m_uid ).child( "displayName" )
                        .setValue( this.m_model.m_displayName );

            } else {
                this.m_model.m_displayName = userDisplayNameValue;
            }

            /* Add facebook ID */
            if( this.m_model.m_firebaseUser != null ) {
                for (UserInfo info : this.m_model.m_firebaseUser.getProviderData()) {
                    if( info.getProviderId().toLowerCase().contains( "facebook" ) ) {
                        UserModel.m_ref.child( "users" ).child( this.m_model.m_uid )
                                .child( "facebookUID" ).setValue( info.getUid() );
                        break;
                    }
                }
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class DisplayNameListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserModel.this.m_displayName = dataSnapshot.getValue( String.class );
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class OwnedEventListener implements ValueEventListener {

        private UserModel m_model;

        OwnedEventListener(UserModel model) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            /* Remove all existing entries */
            this.m_model.m_ownedEvents.clear();

            /* Add all entries from data base */
            for (DataSnapshot ownedEvent: dataSnapshot.getChildren()) {
                this.m_model.m_ownedEvents.add( new OwnedEventEntry(
                        ownedEvent.getKey(),
                        ownedEvent.getValue( String.class )
                ));
            }

            /* Alert Handler */
            UserModelUpdateHandler handler = this.m_model.getHandler();
            if( handler != null)
                handler.userOwnedEventsUpdated();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class SavedEventListener implements ValueEventListener {

        private UserModel m_model;

        SavedEventListener(UserModel model) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            this.m_model.m_savedEvents.clear();

            /* Add all entries from data base */
            for (DataSnapshot savedEvent: dataSnapshot.getChildren()) {
                this.m_model.m_savedEvents.add( new SavedEventEntry(
                        savedEvent.getKey(),
                        savedEvent.getValue( String.class )
                ));
            }

            /* Alert Handler */
            UserModelUpdateHandler handler = this.m_model.getHandler();
            if( handler != null)
                handler.userSavedEventsUpdated();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class DrivesForListener implements ValueEventListener {

        private UserModel m_model;

        DrivesForListener(UserModel model) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            this.m_model.m_drivesFor.clear();
            this.m_model.unsubscribeFromAllNotifications();

            /* Add all entries from data base */
            for (DataSnapshot drivesForEvent: dataSnapshot.getChildren()) {
                this.m_model.m_drivesFor.add( new DrivesForEntry(
                        drivesForEvent.getKey(),
                        drivesForEvent.getValue( String.class )
                ));

                /* Get notifications */
                this.m_model.subscribeToNotifications( drivesForEvent.getKey() );
            }

            /* Alert Handler */
            UserModelUpdateHandler handler = this.m_model.getHandler();
            if( handler != null)
                handler.userDrivesForUpdated();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class RidesEventListener implements ValueEventListener {

        private UserModel m_model;

        RidesEventListener(UserModel model) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_rides.clear();

            /* Add all entries from data base */
            for (DataSnapshot rides: dataSnapshot.getChildren()) {
                this.m_model.m_rides.add( new RidesEntry(
                        rides.getKey(),
                        rides.getValue( String.class )
                ));
            }

            /* Alert Handler */
            UserModelUpdateHandler handler = this.m_model.getHandler();
            if( handler != null)
                handler.userRidesUpdated();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }

    private class DrivesEventListener implements ValueEventListener {

        private UserModel m_model;

        DrivesEventListener(UserModel model) {
            this.m_model = model;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            this.m_model.m_drives.clear();

            /* Add all entries from data base */
            for (DataSnapshot drives: dataSnapshot.getChildren()) {
                this.m_model.m_drives.add( new DrivesEntry(
                        drives.getKey(),
                        drives.getValue( String.class )
                ));
            }

            /* Alert Handler */
            UserModelUpdateHandler handler = this.m_model.getHandler();
            if( handler != null)
                handler.userRidesUpdated();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    }
}
