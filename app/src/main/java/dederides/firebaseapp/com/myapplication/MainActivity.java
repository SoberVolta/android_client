package dederides.firebaseapp.com.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseUser;

import dederides.firebaseapp.com.myapplication.authentication.FacebookAuthHandler;
import dederides.firebaseapp.com.myapplication.authentication.FacebookAuthModel;
import dederides.firebaseapp.com.myapplication.authentication.FirebaseAuthHandler;
import dederides.firebaseapp.com.myapplication.authentication.FirebaseAuthModel;
import dederides.firebaseapp.com.myapplication.data.model.user.UserModel;

/*                                                                                                   */
/* Main Activity Components ***********************************************************************/
/*                                                                                                */

public class MainActivity extends AppCompatActivity implements
        FacebookAuthHandler, FirebaseAuthHandler

{

    private static final String TAG = "MainActivity";

    /* Member Variables *******************************************************/

    /* Authentication Model */
    private FacebookAuthModel facebookAuth;
    private FirebaseAuthModel firebaseAuth;

    /* Application Lifecycle *************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Local Variable */
        LoginButton fb_login_button;

        /* Application Outlets */
        fb_login_button = ( LoginButton ) findViewById( R.id.fb_login_button );

        /* Authentication Initialization */
        this.firebaseAuth = new FirebaseAuthModel( this );
        this.facebookAuth = new FacebookAuthModel( this );
        if( this.facebookAuth.configureFacebookLoginButton( fb_login_button ) ) {
            this.onValidAuthentication( facebookAuth.getAccessToken() );
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.facebookAuth.getCallbackManager().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Facebook Authentication Handler ***************************************/

    @Override
    public void onValidAuthentication(AccessToken accessToken) {

        /* Defensive Programming - Check valid access token */
        if( accessToken == null ) {
            handleNewUser( null );
            return;
        }

        /* Pass facebook token off to firebase authentication model */
        this.firebaseAuth.handleFacebookAccessToken( accessToken );
    }

    @Override
    public void onError(FacebookException error) {

        /* Inform user of error */
        Toast.makeText(
                this,
                "A Facebook Error Occurred. Please Restart The App.",
                Toast.LENGTH_LONG)
                .show();

        /* Log Error */
        Log.e( TAG, error.toString() );
    }

    /* Firebase Authentication Handler ***************************************/

    @Override
    public void handleNewUser(FirebaseUser newUser) {

        /* Check if new user */
        if( newUser != null ) {

            this.setTitle( newUser.getDisplayName() );
            UserModel userModel = new UserModel( newUser, null );

            /* Switch Activity */
            Intent switchActivityIntent = new Intent( this, MainMenu.class );
            switchActivityIntent.putExtra( MainMenu.USER_UID, userModel.getUID() );
            startActivity(switchActivityIntent);

        }
        /* Check if no user signed in */
        else {
            this.setTitle( "Sign In");
        }

    }

    @Override
    public void handleAuthenticationFailure() {

        /* Inform user of error */
        Toast.makeText(
                this,
                "A Firebase Error Occurred. Please Restart The App.",
                Toast.LENGTH_LONG)
                .show();

        /* Log Failure */
        Log.e( TAG, "Firebase Authentication Failure" );
    }
}
