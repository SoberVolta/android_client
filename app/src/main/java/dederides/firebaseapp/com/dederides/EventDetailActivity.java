package dederides.firebaseapp.com.dederides;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Set;

import dederides.firebaseapp.com.dederides.data.model.event.ActiveRidesEntry;
import dederides.firebaseapp.com.dederides.data.model.event.EventModel;
import dederides.firebaseapp.com.dederides.data.model.event.EventModelUpdateHandler;
import dederides.firebaseapp.com.dederides.data.model.event.QueueEntry;
import dederides.firebaseapp.com.dederides.data.model.user.RidesEntry;
import dederides.firebaseapp.com.dederides.data.model.user.UserModel;
import dederides.firebaseapp.com.dederides.data.model.user.UserModelUpdateHandler;

public class EventDetailActivity extends AppCompatActivity implements UserModelUpdateHandler,
        EventModelUpdateHandler {

    public static final String USER_UID = "user_uid_extra";
    public static final String EVENT_ID = "event_id_extra";

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

    private boolean m_userIsInQueue;
    private boolean m_userIsInActiveRide;

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
        this.m_eventModel = new EventModel( this.m_eventID, this );

        this.lbl_eventTitle = ( TextView ) findViewById( R.id.lbl_eventTitle );
        this.lbl_eventLocation = ( TextView ) findViewById( R.id.lbl_eventLocation );
        this.btn_requestRide = ( Button ) findViewById( R.id.btn_requestRide );
        this.btn_offerDrive = ( Button ) findViewById( R.id.btn_offerDrive );
        this.btn_copyEventLink = ( Button ) findViewById( R.id.btn_copyEventLink );
        this.btn_viewDrivers = ( Button ) findViewById( R.id.btn_viewDriveOffers );
        this.btn_disableEvent = ( Button ) findViewById( R.id.btn_disableEvent );
        this.btn_deleteEvent = ( Button ) findViewById( R.id.btn_deleteEvent );

        this.m_userIsInQueue = false;
        this.m_userIsInActiveRide = false;
    }

    /* Button Press Handlers *************************************************/

    public void onRequestRideClick( View view ) {

    }

    public void onOfferDriveClick( View view ) {

    }

    public void onCopyEventLinkClick( View view ) {

    }

    public void onViewDriveOffersClick( View view ) {

    }

    public void onDisableEventClick( View view ) {

    }

    public void onDeleteEventClick( View view ) {

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

    @Override
    public void eventPendingDriversDidChange() {

    }

    @Override
    public void eventDriversDidChange() {

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
