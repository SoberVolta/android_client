package dederides.firebaseapp.com.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import dederides.firebaseapp.com.myapplication.data.model.UserModel;
import dederides.firebaseapp.com.myapplication.data.model.UserModelUpdateHandler;

public class MainMenu extends AppCompatActivity implements UserModelUpdateHandler {

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
    }

    /* User Model Update Handler *********************************************/

    @Override
    public void userOwnedEventsUpdated() {
        this.m_listViewAdapter.notifyDataSetChanged();
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
            return this.m_mainMenu.m_userModel.getOwnedEvents().size();
        }

        @Override
        public Object getItem(int i) {
            return "TEST STRING";
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = LayoutInflater.from( this.m_context );
            @SuppressLint("ViewHolder") View row = layoutInflater.inflate(              // TODO: Fix
                    R.layout.main_menu_listview_row,
                    viewGroup,
                    false
            );

            TextView lbl_rowType = row.findViewById( R.id.lbl_rowType );
            lbl_rowType.setText( "Your Event" );
            lbl_rowType.setTextColor( R.attr.colorPrimary );

            TextView lbl_rowTitle = row.findViewById( R.id.lbl_rowTitle );
            lbl_rowTitle.setText( this.m_mainMenu.m_userModel.getOwnedEvents().get( i ).eventName );

            return row;
        }
    }
}
