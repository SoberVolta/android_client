package dederides.firebaseapp.com.dederides;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import dederides.firebaseapp.com.dederides.data.model.event.EventModel;
import dederides.firebaseapp.com.dederides.data.model.event.EventModelUpdateHandler;
import dederides.firebaseapp.com.dederides.data.model.user.UserModel;
import dederides.firebaseapp.com.dederides.data.model.user.UserModelUpdateHandler;

public class DriveActivity extends AppCompatActivity implements UserModelUpdateHandler, EventModelUpdateHandler {

    public static final String USER_UID_EXTRA = "user_uid_extra";
    public static final String EVENT_ID_EXTRA = "event_id_extra";

    private UserModel m_userModel;
    private EventModel m_eventModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        String userUID;
        String eventID;

        Intent intent = getIntent();
        userUID = intent.getStringExtra( DriveActivity.USER_UID_EXTRA );
        eventID = intent.getStringExtra( DriveActivity.EVENT_ID_EXTRA );

        this.m_userModel = new UserModel( userUID, this );
        this.m_eventModel = new EventModel( eventID, this );
    }

    /* User Model Update Handler *********************************************/

    @Override
    public void userOwnedEventsUpdated() {

    }

    @Override
    public void userSavedEventsUpdated() {

    }

    @Override
    public void userDrivesForUpdated() {

    }

    @Override
    public void userRidesUpdated() {

    }

    @Override
    public void userDrivesUpdated() {

    }

    /* Event Model Update Handler ********************************************/

    @Override
    public void eventNameDidChange() {
        this.setTitle( "Drive for " + this.m_eventModel.getName() );
    }

    @Override
    public void eventLocationDidChange() {

    }

    @Override
    public void eventOwnerDidChange() {

    }

    @Override
    public void eventDisabledDidChange() {

    }

    @Override
    public void eventQueueDidChange() {

    }

    @Override
    public void eventActiveRidesDidChange() {

    }

    @Override
    public void eventPendingDriversDidChange() {

    }

    @Override
    public void eventDriversDidChange() {

    }

    @Override
    public void eventDeleted() {

    }
}
