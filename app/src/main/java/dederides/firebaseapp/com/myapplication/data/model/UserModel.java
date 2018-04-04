package dederides.firebaseapp.com.myapplication.data.model;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserModel {

    private UserModelUpdateHandler m_handler;

    private String m_displayName;
    private String m_uid;

    private DatabaseReference m_ref = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference m_userRef;

    private ArrayList<OwnedEventEntry> m_ownedEvents;

    public UserModel( FirebaseUser user, UserModelUpdateHandler handler ) {

        /* Initial model variables */
        this.m_handler = handler;

        /* Initialize auth variables */
        this.m_displayName = user.getDisplayName();
        this.m_uid = user.getUid();

        this.construct();
    }

    public UserModel( String user_uid, UserModelUpdateHandler handler ) {

        /* Initial model variables */
        this.m_handler = handler;

        /* Initialize auth variables */
        this.m_uid = user_uid;

        this.construct();
    }

    private void construct() {

        /* Initialize database user root */
        this.m_userRef = m_ref.child( "users" ).child( this.m_uid );

        /* Add user to database if not present */
        CreateUserInDatabaseIfAbsent createUser = new CreateUserInDatabaseIfAbsent( this );
        m_userRef.child( "displayName" ).addListenerForSingleValueEvent( createUser );

        /* Initialize Database Populated Variables */
        this.m_ownedEvents = new ArrayList<>();

        /* Add Event Listeners */
        m_userRef.child( "ownedEvents" ).addValueEventListener(
                new OwnedEventListener( this )
        );
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

    private UserModelUpdateHandler getHandler() {
        return this.m_handler;
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

                /* TODO: Add if not present */

            } else {
                this.m_model.m_displayName = userDisplayNameValue;
            }
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
}
