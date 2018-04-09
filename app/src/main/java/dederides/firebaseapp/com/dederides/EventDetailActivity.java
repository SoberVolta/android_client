package dederides.firebaseapp.com.dederides;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import dederides.firebaseapp.com.dederides.data.model.event.ActiveRidesEntry;
import dederides.firebaseapp.com.dederides.data.model.event.DriversEntry;
import dederides.firebaseapp.com.dederides.data.model.event.EventModel;
import dederides.firebaseapp.com.dederides.data.model.event.EventModelUpdateHandler;
import dederides.firebaseapp.com.dederides.data.model.event.PendingDriversEntry;
import dederides.firebaseapp.com.dederides.data.model.event.QueueEntry;
import dederides.firebaseapp.com.dederides.data.model.user.RidesEntry;
import dederides.firebaseapp.com.dederides.data.model.user.UserModel;
import dederides.firebaseapp.com.dederides.data.model.user.UserModelUpdateHandler;

public class EventDetailActivity extends AppCompatActivity implements UserModelUpdateHandler,
        EventModelUpdateHandler {

    public static final String USER_UID = "user_uid_extra";
    public static final String EVENT_ID = "event_id_extra";
    public static final int LOCATION_PERMISSION_CODE = 0;

    /* Member Variables ******************************************************/

    private String m_userUID;
    private UserModel m_userModel;
    private String m_eventID;

    private EventModel m_eventModel;

    /* UI Elements */
    private TextView lbl_eventTitle;
    private TextView lbl_eventLocation;
    private Button btn_requestRide;
    private Button btn_offerDrive;
    private Button btn_copyEventLink;
    private Button btn_viewDrivers;
    private Button btn_disableEvent;
    private Button btn_deleteEvent;

    private OnRequestRideClickListener m_requstRideListener;

    private boolean m_userIsInQueue;
    private boolean m_userIsInActiveRide;
    private boolean m_userHasOfferedDrive;
    private boolean m_userIsActiveDriver;

    private FusedLocationProviderApi m_fusedLocationClient;

    /* Application Lifecycle *************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        this.m_userUID = intent.getStringExtra(EventDetailActivity.USER_UID);
        this.m_eventID = intent.getStringExtra(EventDetailActivity.EVENT_ID);

        this.m_userModel = new UserModel(this.m_userUID, this);
        this.m_eventModel = new EventModel(this.m_eventID, this);

        this.lbl_eventTitle = (TextView) findViewById(R.id.lbl_eventTitle);
        this.lbl_eventLocation = (TextView) findViewById(R.id.lbl_eventLocation);
        this.btn_requestRide = (Button) findViewById(R.id.btn_requestRide);
        this.btn_offerDrive = (Button) findViewById(R.id.btn_offerDrive);
        this.btn_copyEventLink = (Button) findViewById(R.id.btn_copyEventLink);
        this.btn_viewDrivers = (Button) findViewById(R.id.btn_viewDriveOffers);
        this.btn_disableEvent = (Button) findViewById(R.id.btn_disableEvent);
        this.btn_deleteEvent = (Button) findViewById(R.id.btn_deleteEvent);

        this.m_requstRideListener = new OnRequestRideClickListener( this );

        this.m_userIsInQueue = false;
        this.m_userIsInActiveRide = false;
        this.m_userHasOfferedDrive = false;
        this.m_userIsActiveDriver = false;

        this.m_fusedLocationClient = LocationServices.FusedLocationApi;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        switch ( requestCode ) {
            case LOCATION_PERMISSION_CODE: {

                /* Check if permission was granted */
                if( grantResults.length > 0
                        && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED ) {

                    this.m_requstRideListener.onClick(null, 0);

                } else {

                    Toast.makeText(
                            this,
                            "Unable to request ride. Location Services Required.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        }
    }

    /* Button Press Handlers *************************************************/

    public void onRequestRideClick(View view) {

        if( this.m_userIsInActiveRide ) {
            return;
        }

        if( this.m_userIsInQueue ) {

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cancel Ride Request?");
            builder.setMessage("Are you don't want a ride to "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Do nothing on cancel */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("Cancel Ride", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    EventDetailActivity.this.m_eventModel.cancelRideRequest(
                            EventDetailActivity.this.m_userModel.getUID()
                    );

                }
            });
            builder.create().show();

        } else {

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Request a Ride?");
            builder.setMessage("Are you sure you want to request a ride to "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Do nothing on cancel */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("Request Ride", this.m_requstRideListener);
            builder.create().show();
        }
    }

    private class OnRequestRideClickListener implements DialogInterface.OnClickListener {

        EventDetailActivity m_activity;

        OnRequestRideClickListener( EventDetailActivity activity ) {
            this.m_activity = activity;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

            /* Local Variables */
            LocationManager locationManager;
            Location location;

            /* If location permission not granted */
            if (ActivityCompat.checkSelfPermission(EventDetailActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    ) {

                /* Async Request Permission */
                ActivityCompat.requestPermissions(
                        this.m_activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        EventDetailActivity.LOCATION_PERMISSION_CODE
                );

                /* Catch in onRequestPermissionResult */
                return;
            }

            /* Get Users Location */
            locationManager = ( (LocationManager) EventDetailActivity.this.getSystemService(
                    EventDetailActivity.LOCATION_SERVICE
            ) );

            if (locationManager != null) {
                location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );

                /* Enqueue new ride request */
                this.m_activity.m_eventModel.enqueueNewRideRequest(
                        this.m_activity.m_userModel.getUID(),
                        location.getLatitude(),
                        location.getLongitude()
                );

            } else {

                /* Alert user of error */
                Toast.makeText(
                        EventDetailActivity.this,
                        "Unable to request ride. Location Not Available.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    public void onOfferDriveClick( View view ) {

        /* If user has already offered to drive */
        if ( this.m_userHasOfferedDrive ) {

             /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cancel Drive Offer?");
            builder.setMessage("Are you sure you don't want to drive for "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Do nothing on cancel */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("Cancel Drive Offer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EventDetailActivity.this.m_eventModel.cancelPendingDriveOffer(
                            EventDetailActivity.this.m_userModel.getUID()
                    );
                }
            });
            builder.create().show();

        }
        /* If user is already an active driver */
        else if ( this.m_userIsActiveDriver ) {

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cancel Drive Offer?");
            builder.setMessage("Are you sure you want to stop driving for "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Do nothing on cancel */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("Stop Driving", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EventDetailActivity.this.m_eventModel.removeDriverFromEvent(
                            EventDetailActivity.this.m_userModel.getUID()
                    );
                }
            });
            builder.create().show();

        } else {

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Offer to Drive?");
            builder.setMessage("Are you sure you want to offer to drive for "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Do nothing on cancel */
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("Offer to Drive", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EventDetailActivity.this.m_eventModel.addDriveOffer(
                            EventDetailActivity.this.m_userModel.getUID(),
                            EventDetailActivity.this.m_userModel.getDisplayName()
                    );
                }
            });
            builder.create().show();
        }
    }

    public void onCopyEventLinkClick( View view ) {

        /* Copy Link to Clipboard */
        ClipboardManager clipboard
                = ( ClipboardManager ) getSystemService(Context.CLIPBOARD_SERVICE );
        ClipData clip = ClipData.newPlainText(
                "",
                "http://dede-rides.firebaseapp.com/event/index.html?id="
                        + this.m_eventModel.getEventID()
        );
        clipboard.setPrimaryClip(clip);

        /* Alert User */
        Toast.makeText(
                this,
                "Link Copied to Clipboard",
                Toast.LENGTH_LONG
        ).show();
    }

    public void onViewDriveOffersClick( View view ) {

    }

    public void onDisableEventClick( View view ) {

        if ( this.m_eventModel.isDisabled() ) {
            this.m_eventModel.enableEvent();
        } else {
            this.m_eventModel.disableEvent();
        }

    }

    public void onDeleteEventClick( View view ) {

        /* Display confirmation dialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Event?");
        builder.setMessage("Are you sure you want to delete "
                + this.m_eventModel.getName() + "?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /* Do nothing on cancel */
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EventDetailActivity.this.m_eventModel.removeDriverFromEvent(
                        EventDetailActivity.this.m_userModel.getUID()
                );
            }
        });
        builder.create().show();

    }

    /* Event Model Update Handler ********************************************/

    @Override
    public void eventNameDidChange() {

        this.setTitle( m_eventModel.getName() );
        this.lbl_eventTitle.setText( m_eventModel.getName() );

    }

    @Override
    public void eventLocationDidChange() {

        this.lbl_eventLocation.setText( m_eventModel.getLocation() );

    }

    @Override
    public void eventDisabledDidChange() {

        if( this.m_eventModel.isDisabled() ) {
            if( !this.m_eventModel.getOwnerUID().equals( this.m_userUID ) ) {
                onBackPressed();
            } else {
                this.btn_disableEvent.setText( "Enable Event" );
            }
        } else {
            this.btn_disableEvent.setText( "Disable Event" );
        }

    }

    @Override
    public void eventOwnerDidChange() {

        if ( this.m_userUID.equals( m_eventModel.getOwnerUID() )) {

            this.btn_copyEventLink.setVisibility( View.VISIBLE );
            this.btn_viewDrivers.setVisibility( View.VISIBLE );
            this.btn_disableEvent.setVisibility( View.VISIBLE );
            this.btn_deleteEvent.setVisibility( View.VISIBLE );

        } else {

            this.btn_copyEventLink.setVisibility( View.INVISIBLE );
            this.btn_viewDrivers.setVisibility( View.INVISIBLE );
            this.btn_disableEvent.setVisibility( View.INVISIBLE );
            this.btn_deleteEvent.setVisibility( View.INVISIBLE );

        }
    }

    private void updateRequestRideUIElement() {

        /* Assume user is not in queue or active ride */
        this.m_userIsInQueue = false;
        this.m_userIsInActiveRide = false;
        this.btn_requestRide.setEnabled( true );
        this.btn_requestRide.setText( "Request a Ride" );
        this.btn_requestRide.setTextColor( Color.BLACK );

        /* Check if rider is in queue */
        for (QueueEntry queueEntry : this.m_eventModel.getQueue()) {
            if( queueEntry.riderUID.equals( this.m_userModel.getUID() ) ) {

                this.m_userIsInQueue = true;
                this.btn_requestRide.setText( "Cancel Ride Request" );
                this.btn_requestRide.setTextColor( Color.RED );
                return;
            }
        }

        /* Check if rider is in an active ride */
        for (ActiveRidesEntry activeRide : this.m_eventModel.getActiveRides()) {
            for (RidesEntry userRide : this.m_userModel.getRides() ) {
                if ( activeRide.rideID.equals( userRide.rideID )) {

                    this.m_userIsInActiveRide = true;
                    this.btn_requestRide.setEnabled( false );
                    this.btn_requestRide.setTextColor( Color.GRAY );
                    return;

                }
            }
        }
    }

    @Override
    public void eventQueueDidChange() {
        updateRequestRideUIElement();
    }

    @Override
    public void eventActiveRidesDidChange() {
        updateRequestRideUIElement();
    }

    private void checkForUserDrivingForEvent() {

        /* Assume user is not driving for event */
        this.m_userHasOfferedDrive = false;
        this.m_userIsActiveDriver = false;
        this.btn_offerDrive.setTextColor( Color.BLACK );
        this.btn_offerDrive.setText( "Offer to Drive" );

        /* Check if this user has requested to drive */
        for (PendingDriversEntry pendingDriver : this.m_eventModel.getPendingDrivers()) {
            if( pendingDriver.driverUID.equals( this.m_userModel.getUID() )) {

                this.m_userHasOfferedDrive = true;
                this.btn_offerDrive.setTextColor( Color.RED );
                this.btn_offerDrive.setText( "Cancel Drive Offer" );
                return;
            }
        }

        /* Check if user is active driver */
        for (DriversEntry driver : this.m_eventModel.getDrivers()) {
            if( driver.driverUID.equals( this.m_userModel.getUID() )) {

                this.m_userIsActiveDriver = true;
                this.btn_offerDrive.setTextColor( Color.RED );
                this.btn_offerDrive.setText( "Cancel Drive Offer" );
                return;
            }
        }
    }

    @Override
    public void eventPendingDriversDidChange() {
        checkForUserDrivingForEvent();
    }

    @Override
    public void eventDriversDidChange() {
        checkForUserDrivingForEvent();
    }

    @Override
    public void eventDeleted() {
        onBackPressed();
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

    @Override
    public void userRidesUpdated() {
        updateRequestRideUIElement();
    }

    @Override
    public void userDrivesUpdated() {

    }
}
