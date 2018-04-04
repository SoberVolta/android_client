package dederides.firebaseapp.com.myapplication;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import dederides.firebaseapp.com.myapplication.data.model.UserModel;
import dederides.firebaseapp.com.myapplication.data.model.UserModelUpdateHandler;

public class EventDetailActivity extends AppCompatActivity implements UserModelUpdateHandler {

    public static final String USER_UID = "user_uid_extra";
    public static final String EVENT_ID = "event_id_extra";

    /* Member Variables ******************************************************/

    private String m_userUID;
    private UserModel m_userModel;
    private String m_eventID;

    /* Application Lifecycle *************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        this.m_userUID = intent.getStringExtra( EventDetailActivity.USER_UID );
        this.m_eventID = intent.getStringExtra( EventDetailActivity.EVENT_ID );

        this.m_userModel = new UserModel( this.m_userUID, this );

        this.setTitle( this.m_eventID );
    }

    /* User Model Event Handler **********************************************/

    @Override
    public void userOwnedEventsUpdated() {

    }

    @Override
    public void userSavedEventsUpdated() {

    }

    @Override
    public void userDrivesForUpdated() {

    }
}
