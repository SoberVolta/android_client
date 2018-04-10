package dederides.firebaseapp.com.dederides;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import dederides.firebaseapp.com.dederides.data.model.event.EventModel;
import dederides.firebaseapp.com.dederides.data.model.event.EventModelUpdateHandler;

public class ViewDriversActivity extends AppCompatActivity implements EventModelUpdateHandler,
        AdapterView.OnItemClickListener {

    public static final String EVENT_ID_EXTRA = "event_id_extra";

    private String m_eventID;
    private EventModel m_eventModel;
    private ListView m_listView;
    private DriversAdapter m_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drivers);

        Intent intent = getIntent();
        this.m_eventID = intent.getStringExtra( EVENT_ID_EXTRA );

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.m_listView = ( ListView ) findViewById( R.id.listview_primaryListView );
        this.m_adapter = new DriversAdapter( this );

        this.m_eventModel = new EventModel( this.m_eventID, this );

        this.m_listView.setAdapter( this.m_adapter );
        this.m_listView.setOnItemClickListener( this );

    }

    @Override
    public void eventNameDidChange() {
        this.setTitle( this.m_eventModel.getName() + " drivers" );
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
        this.m_adapter.notifyDataSetChanged();
    }

    @Override
    public void eventDriversDidChange() {
        this.m_adapter.notifyDataSetChanged();
    }

    @Override
    public void eventDeleted() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        final int pendingDriversCount = this.m_eventModel.getPendingDrivers().size();
        int driversCount = this.m_eventModel.getDrivers().size();
        String driverName;
        final int idx = i;

        if ( i < pendingDriversCount ) {

            driverName = this.m_eventModel.getPendingDrivers().get( i ).driverDisplayName;

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pending Driver: " + driverName + "?");
            builder.setMessage("Do you want " + driverName + " to drive for "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Reject Offer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int j) {
                    ViewDriversActivity.this.m_eventModel.rejectPendingRideOffer(
                            ViewDriversActivity.this.m_eventModel.getPendingDrivers().get( idx ).driverUID
                    );
                }
            });
            builder.setPositiveButton("Accept Offer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int j) {
                    ViewDriversActivity.this.m_eventModel.acceptDriveOffer(
                            ViewDriversActivity.this.m_eventModel.getPendingDrivers().get( idx ).driverUID,
                            ViewDriversActivity.this.m_eventModel.getPendingDrivers().get( idx ).driverDisplayName
                    );
                }
            });
            builder.create().show();

        } else if ( i < pendingDriversCount + driversCount ) {

            driverName = this.m_eventModel.getDrivers().get( i - pendingDriversCount ).driverDisplayName;

            /* Display confirmation dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Active Driver: " + driverName + "?");
            builder.setMessage("Do you want to remove " + driverName + " from driving for "
                    + this.m_eventModel.getName() + "?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setPositiveButton("Remove Offer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int j) {
                   ViewDriversActivity.this.m_eventModel.removeDriverFromEvent(
                           ViewDriversActivity.this.m_eventModel.getDrivers().get(
                                   idx - pendingDriversCount
                           ).driverUID
                   );
                }
            });
            builder.create().show();

        }

    }

    private class DriversAdapter extends BaseAdapter {

        private ViewDriversActivity m_activity;

        DriversAdapter( ViewDriversActivity activity ) {
            this.m_activity = activity;
        }

        @Override
        public int getCount() {
            return this.m_activity.m_eventModel.getPendingDrivers().size()
                    + this.m_activity.m_eventModel.getDrivers().size();
        }

        @Override
        public Object getItem(int i) {
            return "TEST STRING";
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View row = null;
            int pendingDriversCount = this.m_activity.m_eventModel.getPendingDrivers().size();
            int driversCount = this.m_activity.m_eventModel.getDrivers().size();

            LayoutInflater layoutInflater = LayoutInflater.from(this.m_activity);
            row = layoutInflater.inflate(                                               // TODO: Fix
                    R.layout.driver_listview_row,
                    viewGroup,
                    false
            );
            TextView lbl_driverStatus = row.findViewById(R.id.lbl_driverStatus);
            TextView lbl_driverName = row.findViewById(R.id.lbl_driverName);

            if( i < pendingDriversCount ) {

                lbl_driverStatus.setText( "Pending:" );
                lbl_driverStatus.setTextColor( 0xFFFFD700 );

                lbl_driverName.setText(
                        this.m_activity.m_eventModel.getPendingDrivers().get( i ).driverDisplayName
                );

            } else if ( i < ( pendingDriversCount + driversCount ) ) {

                lbl_driverStatus.setText( "Active:" );
                lbl_driverStatus.setTextColor( 0xFF228B22 );

                lbl_driverName.setText(
                        this.m_activity.m_eventModel.getDrivers().get(
                                i - pendingDriversCount
                        ).driverDisplayName
                );

            }

            return row;
        }

    }
}
