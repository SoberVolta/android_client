package dederides.firebaseapp.com.dederides;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import dederides.firebaseapp.com.dederides.data.model.user.UserModel;
import dederides.firebaseapp.com.dederides.data.model.user.UserModelUpdateHandler;

public class MainMenu extends AppCompatActivity implements UserModelUpdateHandler,
        AdapterView.OnItemClickListener {

    public static final String USER_UID = "user_uid_key";

    /* Member Variables ******************************************************/

    /* Model Variables */
    private String m_userUID;
    private UserModel m_userModel;

    /* UI Variables */
    private ListView m_listView;
    private MainMenuAdapter m_listViewAdapter;

    /* Application Lifecycle *************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Intent intent = getIntent();
        this.m_userUID = intent.getStringExtra( MainMenu.USER_UID );

        this.m_userModel = new UserModel( this.m_userUID, this );

        this.m_listView = ( ListView ) findViewById( R.id.listView );
        this.m_listViewAdapter = new MainMenuAdapter( this );
        this.m_listView.setAdapter( this.m_listViewAdapter );
        this.m_listView.setOnItemClickListener( this );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_create:

                Intent switchToCreateEvent = new Intent( this, CreateEventActivity.class );
                switchToCreateEvent.putExtra( CreateEventActivity.USER_UID_EXTRA, this.m_userUID );
                startActivity( switchToCreateEvent );

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* User Model Update Handler *********************************************/

    @Override
    public void userOwnedEventsUpdated() {
        this.m_listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void userSavedEventsUpdated() {
        this.m_listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void userDrivesForUpdated() {
        this.m_listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void userRidesUpdated() {
        this.m_listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void userDrivesUpdated() {

    }

    /* On Item Click Listener ************************************************/

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        int ownedEventsLength = this.m_userModel.getOwnedEvents().size();
        int savedEventsLength = this.m_userModel.getSavedEvents().size();
        int drivesForLength = this.m_userModel.getDrivesFor().size();
        int ridesLength = this.m_userModel.getRides().size();
        String eventID;
        String rideID;
        String eventName;

        if( i < ownedEventsLength ) {

            eventID = this.m_userModel.getOwnedEvents().get( i ).eventID;

            /* Switch Activity */
            Intent switchActivityIntent = new Intent( this, EventDetailActivity.class );
            switchActivityIntent.putExtra( EventDetailActivity.USER_UID, this.m_userUID );
            switchActivityIntent.putExtra( EventDetailActivity.EVENT_ID, eventID );
            startActivity(switchActivityIntent);

        } else if( i < ownedEventsLength + savedEventsLength ) {

            eventID = this.m_userModel.getSavedEvents().get( i - ownedEventsLength ).eventID;

            /* Switch Activity */
            Intent switchActivityIntent = new Intent( this, EventDetailActivity.class );
            switchActivityIntent.putExtra( EventDetailActivity.USER_UID, this.m_userUID );
            switchActivityIntent.putExtra( EventDetailActivity.EVENT_ID, eventID );
            startActivity(switchActivityIntent);

        } else if( i < ownedEventsLength + savedEventsLength + drivesForLength ) {

            eventID = this.m_userModel.getDrivesFor().get( i - ownedEventsLength - savedEventsLength ).eventID;

            Intent switchToDriveDetailActivity
                    = new Intent( this, DriveActivity.class );
            switchToDriveDetailActivity.putExtra(
                    DriveActivity.USER_UID_EXTRA,
                    this.m_userModel.getUID()
            );
            switchToDriveDetailActivity.putExtra(
                    DriveActivity.EVENT_ID_EXTRA,
                    eventID
            );
            startActivity( switchToDriveDetailActivity );

        } else if ( i < ownedEventsLength + savedEventsLength + drivesForLength + ridesLength ) {

            rideID = this.m_userModel.getRides().get(
                    i - ownedEventsLength - savedEventsLength - drivesForLength
            ).rideID;
            eventName = this.m_userModel.getRides().get(
                    i - ownedEventsLength - savedEventsLength - drivesForLength
            ).eventName;

            Intent switchToRideDetail = new Intent( this, RideDetailActivity.class );
            switchToRideDetail.putExtra( RideDetailActivity.RIDE_ID_EXTRA, rideID );
            switchToRideDetail.putExtra( RideDetailActivity.EVENT_NAME_EXTRA, eventName );
            startActivity( switchToRideDetail );

        }
    }

    /* List View Adapter *****************************************************/

    private class MainMenuAdapter extends BaseAdapter {

        private Context m_context;
        private MainMenu m_mainMenu;

        MainMenuAdapter( MainMenu mainMenu ) {

            this.m_context = mainMenu;
            this.m_mainMenu = mainMenu;

        }

        @Override
        public int getCount() {
            return  this.m_mainMenu.m_userModel.getOwnedEvents().size() +
                    this.m_mainMenu.m_userModel.getSavedEvents().size() +
                    this.m_mainMenu.m_userModel.getDrivesFor().size() +
                    this.m_mainMenu.m_userModel.getRides().size();
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
            int ownedEventsLength = this.m_mainMenu.m_userModel.getOwnedEvents().size();
            int savedEventsLength = this.m_mainMenu.m_userModel.getSavedEvents().size();
            int drivesForLength = this.m_mainMenu.m_userModel.getDrivesFor().size();
            int ridesLength = this.m_mainMenu.m_userModel.getRides().size();

            LayoutInflater layoutInflater = LayoutInflater.from(this.m_context);
            row = layoutInflater.inflate(                                               // TODO: Fix
                    R.layout.main_menu_listview_row,
                    viewGroup,
                    false
            );
            TextView lbl_rowType = row.findViewById(R.id.lbl_rowType);
            TextView lbl_rowTitle = row.findViewById(R.id.lbl_rowTitle);

            if( i < ownedEventsLength ) {

                lbl_rowType.setText("Your Event");
                lbl_rowType.setTextColor(R.attr.colorPrimary);

                lbl_rowTitle.setText(this.m_mainMenu.m_userModel.getOwnedEvents().get(i).eventName);

            } else if ( i < ( ownedEventsLength + savedEventsLength )) {

                lbl_rowType.setText( "Saved Event" );
                lbl_rowType.setTextColor( 0xFFFFD700 );

                lbl_rowTitle.setText(this.m_mainMenu.m_userModel.getSavedEvents().get(i - ownedEventsLength).eventName);

            } else if ( i < ( ownedEventsLength + savedEventsLength + drivesForLength )) {

                lbl_rowType.setText( "You Drive For" );
                lbl_rowType.setTextColor( 0xFFFF0000 );

                lbl_rowTitle.setText(this.m_mainMenu.m_userModel.getDrivesFor().get(i - ownedEventsLength - savedEventsLength).eventName);

            } else if ( i < ( ownedEventsLength + savedEventsLength + drivesForLength + ridesLength )) {

                lbl_rowType.setText( "In a Ride" );
                lbl_rowType.setTextColor( 0xFF228b22 );

                lbl_rowTitle.setText(this.m_mainMenu.m_userModel.getRides().get(
                        i - ownedEventsLength - savedEventsLength - drivesForLength ).eventName
                );

            }

            return row;
        }
    }
}
