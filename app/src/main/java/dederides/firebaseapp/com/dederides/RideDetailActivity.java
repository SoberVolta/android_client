package dederides.firebaseapp.com.dederides;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dederides.firebaseapp.com.dederides.data.model.ride.RideModel;
import dederides.firebaseapp.com.dederides.data.model.ride.RideModelUpdateHandler;

public class RideDetailActivity extends AppCompatActivity implements RideModelUpdateHandler {

    public static final String RIDE_ID_EXTRA = "ride_id_extra";
    public static final String EVENT_NAME_EXTRA = "event_name_extra";

    private RideModel m_rideModel;
    private String m_eventName;

    private TextView lbl_eventName;
    private TextView lbl_rideStatus;
    private Button  btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        Intent intent = getIntent();
        String rideID = intent.getStringExtra( RideDetailActivity.RIDE_ID_EXTRA );
        this.m_rideModel = new RideModel( rideID, this );
        this.m_eventName = intent.getStringExtra( RideDetailActivity.EVENT_NAME_EXTRA );

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.lbl_eventName = ( TextView ) findViewById( R.id.lbl_eventName );
        this.lbl_rideStatus = ( TextView ) findViewById( R.id.lbl_rideStatus );
        this.btn_cancel = ( Button ) findViewById( R.id.btn_cancel );

        this.lbl_eventName.setText( this.m_eventName );
    }

    public void onCancelClicked( View view ) {

        switch ( this.m_rideModel.getStatus() ) {
            case 0: {

                /* Display confirmation dialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Cancel Ride Request?");
                builder.setMessage("Are you don't want a ride to "
                        + this.m_eventName + "?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    /* Do nothing on cancel */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setPositiveButton("Cancel Ride", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    RideDetailActivity.this.m_rideModel.cancelRideRequest();

                    }
                });
                builder.create().show();

            }
            break;
            case 1: {

                Toast.makeText(
                        this,
                        "Cannot cancel ride when driver is already on their way.",
                        Toast.LENGTH_LONG
                ).show();

            }
            break;
        }
    }

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
        switch ( this.m_rideModel.getStatus() ) {
            case 0: {

                this.lbl_rideStatus.setText( "Ride Requested" );

                this.btn_cancel.setText( "Cancel Ride Request" );
                this.btn_cancel.setTextColor( Color.RED );
                this.btn_cancel.setEnabled( true );

            }
            break;
            case 1: {
                this.lbl_rideStatus.setText( "Driver en Route" );

                this.btn_cancel.setText( "Cannot Cancel When Driver en Route" );
                this.btn_cancel.setTextColor( Color.GRAY );
                this.btn_cancel.setEnabled( false );

            }
            break;
        }
    }

    @Override
    public void rideLocationDidChange() {

    }

    @Override
    public void rideWasRemoved() {
        onBackPressed();
    }
}
