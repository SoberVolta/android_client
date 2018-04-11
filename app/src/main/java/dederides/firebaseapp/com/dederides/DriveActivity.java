package dederides.firebaseapp.com.dederides;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.Drive;

import java.util.Locale;

import dederides.firebaseapp.com.dederides.data.model.event.ActiveRidesEntry;
import dederides.firebaseapp.com.dederides.data.model.event.EventModel;
import dederides.firebaseapp.com.dederides.data.model.event.EventModelUpdateHandler;
import dederides.firebaseapp.com.dederides.data.model.event.QueueEntry;
import dederides.firebaseapp.com.dederides.data.model.ride.RideModel;
import dederides.firebaseapp.com.dederides.data.model.ride.RideModelUpdateHandler;
import dederides.firebaseapp.com.dederides.data.model.user.DrivesEntry;
import dederides.firebaseapp.com.dederides.data.model.user.UserModel;
import dederides.firebaseapp.com.dederides.data.model.user.UserModelUpdateHandler;

public class DriveActivity extends AppCompatActivity implements UserModelUpdateHandler,
        EventModelUpdateHandler, RideModelUpdateHandler {

    public static final String USER_UID_EXTRA = "user_uid_extra";
    public static final String EVENT_ID_EXTRA = "event_id_extra";

    private UserModel m_userModel;
    private EventModel m_eventModel;
    private RideModel m_activeRide;

    private TextView lbl_eventName;
    private TextView lbl_eventLocation;
    private Button btn_popQueue;
    private Button btn_getRiderLocation;
    private Button btn_negate;

    private boolean m_userIsInActiveDrive;

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

        this.lbl_eventName = ( TextView ) findViewById( R.id.lbl_eventName );
        this.lbl_eventLocation = ( TextView ) findViewById( R.id.lbl_eventLocation );
        this.btn_popQueue = ( Button ) findViewById( R.id.btn_popQueue );
        this.btn_getRiderLocation = ( Button ) findViewById( R.id.btn_getLocation );
        this.btn_negate = ( Button ) findViewById( R.id.btn_negate );

        this.m_userIsInActiveDrive = false;
    }

    private void updateButtons() {

        this.m_userIsInActiveDrive = false;
        this.m_activeRide = null;
        this.btn_popQueue.setEnabled( false );
        this.btn_popQueue.setTextColor( Color.BLACK );
        this.btn_getRiderLocation.setEnabled( false );
        this.btn_getRiderLocation.setTextColor( Color.GRAY );
        this.btn_negate.setTextColor( Color.RED );

        /* Check if user is in active drive for this event */
        for (ActiveRidesEntry activeRideEntry : this.m_eventModel.getActiveRides()) {
            if( activeRideEntry.driverUID.equals( this.m_userModel.getUID() ) ) {

                this.m_userIsInActiveDrive = true;
                this.m_activeRide = new RideModel( activeRideEntry.rideID, this );

                this.btn_popQueue.setEnabled( false );
                this.btn_popQueue.setTextColor( Color.GRAY );
                this.btn_popQueue.setText( "You Are Currently Driving Someone" );

                this.btn_getRiderLocation.setEnabled( true );
                this.btn_getRiderLocation.setTextColor( Color.BLACK );

                this.btn_negate.setText( "End Current Drive" );

                break;
            }
        }

        /* Check if queue is not empty */
        if( !this.m_userIsInActiveDrive ) {

            this.btn_negate.setText( "Cancel Drive Offer" );

            if( this.m_eventModel.getQueue().size() > 0 ) {

                /* Queue is not empty */
                this.btn_popQueue.setEnabled( true );
                this.btn_popQueue.setTextColor( Color.BLACK );
                this.btn_popQueue.setText( "Take Next Rider in Queue" );

            } else {

                /* Queue is empty */
                this.btn_popQueue.setEnabled( false );
                this.btn_popQueue.setTextColor( Color.GRAY );
                this.btn_popQueue.setText( "There is No One in the Event Queue" );

            }
        }
    }

    /* Button Listeners ******************************************************/

    public void onQueuePopClicked( View view ) {

        if( this.m_userIsInActiveDrive || this.m_activeRide != null ) {

            if (this.m_activeRide == null) {
                Toast.makeText(
                        this,
                        "Cannot take another drive",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
        }

         /* Display confirmation dialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Get Next Rider");
        builder.setMessage("Are you sure you want to get the next rider?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /* Do nothing on cancel */
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton("Get Next Rider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DriveActivity.this.m_eventModel.takeNextRideInQueue(
                        DriveActivity.this.m_userModel.getUID()
                );
            }
        });
        builder.create().show();


    }

    public void onGetRiderLocationClicked( View view ) {

        if ( this.m_activeRide == null ) {
            Toast.makeText(
                    this,
                    "Not In Current Drive",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if ( this.m_activeRide.getLatitude() == null || this.m_activeRide.getLongitude() == null ) {

            Toast.makeText(
                    this,
                    "Cannot Get Rider's Location",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        double lat = this.m_activeRide.getLatitude();
        double lon = this.m_activeRide.getLongitude();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lon ));
        startActivity(intent);
    }

    public void onNegateClick( View view ) {

        if( this.m_activeRide != null ) {

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("End Current Drive");
            builder.setMessage("Are you sure the current drive is complete?" );
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Do nothing on cancel */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("End Drive", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    DriveActivity.this.m_activeRide.endActiveRide();

                }
            });
            builder.create().show();

        } else {

            Toast.makeText(
                    this,
                    "Resend Drive Offer",
                    Toast.LENGTH_LONG
            ).show();

        }

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
        this.lbl_eventName.setText( this.m_eventModel.getName() );
    }

    @Override
    public void eventLocationDidChange() {
        this.lbl_eventLocation.setText( this.m_eventModel.getLocation() );
    }

    @Override
    public void eventOwnerDidChange() {

    }

    @Override
    public void eventDisabledDidChange() {

    }

    @Override
    public void eventQueueDidChange() {
        this.updateButtons();
    }

    @Override
    public void eventActiveRidesDidChange() {
        this.updateButtons();
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

    /* Ride Model Update Handler *********************************************/

    @Override
    public void rideEventDidChange() {

    }

    @Override
    public void rideRiderDidChange() {

    }

    @Override
    public void rideDriverDidChange() {

    }

    @Override
    public void rideStatusDidChange() {

    }

    @Override
    public void rideLocationDidChange() {

    }

    @Override
    public void rideWasRemoved() {

    }
}
