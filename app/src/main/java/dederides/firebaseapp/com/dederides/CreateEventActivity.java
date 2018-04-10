package dederides.firebaseapp.com.dederides;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import dederides.firebaseapp.com.dederides.data.model.event.EventModel;

public class CreateEventActivity extends AppCompatActivity {

    public static final String USER_UID_EXTRA = "user_uid_extra";

    private String m_userUID;
    private EditText tf_eventName;
    private EditText tf_eventLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Intent intent = getIntent();
        m_userUID = intent.getStringExtra( CreateEventActivity.USER_UID_EXTRA );

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.tf_eventName = ( EditText ) findViewById( R.id.tf_eventName );
        this.tf_eventLocation = ( EditText ) findViewById( R.id.tf_eventLocation );
    }

    public void onCreateEventClicked( View view ) {

        final String eventName = this.tf_eventName.getText().toString();
        final String eventLocation = this.tf_eventLocation.getText().toString();
        final String ownerUID = this.m_userUID;

        /* Check valid event name */
        if( eventName.length() == 0 ) {

            Toast.makeText(
                    this,
                    "Please enter a valid event name.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        /* Check valid event location */
        if ( eventLocation.length() == 0 ) {

            Toast.makeText(
                    this,
                    "Please enter a value for the event location.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        /* Display confirmation dialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Event?");
        builder.setMessage("Are you sure you want to create the event '"
                + eventName + "' at '" + eventLocation + "'?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /* Do nothing on cancel */
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton("Create Event", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if ( EventModel.createEvent( eventName, eventLocation, ownerUID ) ) {
                    onBackPressed();
                } else {
                    Toast.makeText(
                            CreateEventActivity.this,
                            "Error creating event.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
        builder.create().show();

    }

    public void onCancelClicked( View view ) {
        onBackPressed();
    }
}
