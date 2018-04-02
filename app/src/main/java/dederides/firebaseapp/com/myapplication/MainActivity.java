package dederides.firebaseapp.com.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

interface FB_Authentication_Handler {

    void onValidAuthentication( AccessToken accessToken );
    void onError( FacebookException error );

}

/*                                                                                                */
/* Facebook Auth Components ***********************************************************************/
/*                                                                                                */

class dede_FaceBookAuth implements FacebookCallback<LoginResult>  {

    /* Class Constants */
    private static final String EMAIL = "email";
    private static final String PUBLIC_PROFILE = "public_profile";

    /* Member Variables ******************************************************/

    private FB_Authentication_Handler m_handler;
    private CallbackManager m_callbackManager;
    private AccessToken m_accessToken = null;
    private FacebookException m_error = null;

     /* Constructor **********************************************************/

    dede_FaceBookAuth( FB_Authentication_Handler handler ) {

        this.m_handler = handler;
        this.m_callbackManager = CallbackManager.Factory.create();
    }

    /* Action Items **********************************************************/

    void registerCallback() {

        LoginManager.getInstance().registerCallback( this.getCallbackManager(), this );

    }

    void configureFacebookLoginButton(LoginButton fb_login_button) {

        fb_login_button.setReadPermissions(
                EMAIL,
                PUBLIC_PROFILE
        );
        fb_login_button.registerCallback( this.m_callbackManager, this );

        this.registerCallback();
    }


    /* Getters and Setters ***************************************************/

    CallbackManager getCallbackManager() {
        return m_callbackManager;
    }

    public AccessToken getAccessToken() {
        return m_accessToken;
    }

    public FacebookException getError() {
        return m_error;
    }

/* FacebookCallback **********************************************************/

    @Override
    public void onSuccess(LoginResult loginResult) {

        this.m_accessToken = loginResult.getAccessToken();
        this.m_error = null;

        m_handler.onValidAuthentication( this.m_accessToken );
    }

    @Override
    public void onCancel() {
        // Intentionally do nothing
    }

    @Override
    public void onError(FacebookException error) {

        this.m_error = error;
        this.m_accessToken = null;

        m_handler.onError( this.m_error );
    }
}

interface FirebaseAuthenticationHandler {
    void handleNewUser( FirebaseUser newUser );

    void handleAuthenticationFailure();
}

class FirebaseAuthenticationModel implements OnCompleteListener<AuthResult>{

    private static final String TAG = "FirAuthModel";

    private FirebaseAuth m_auth;
    private FirebaseAuthenticationHandler m_handler = null;

    FirebaseAuthenticationModel( FirebaseAuthenticationHandler handler ) {
        this.m_auth = FirebaseAuth.getInstance();
        this.m_handler = handler;
    }

    public void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        m_auth.signInWithCredential(credential).addOnCompleteListener( this );
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {

        if (task.isSuccessful()) {
            Log.d(TAG, "signInWithCredential:success");

            this.m_handler.handleNewUser( m_auth.getCurrentUser() );

        } else {

            this.m_handler.handleAuthenticationFailure();
        }
    }
}

/*                                                                                                   */
/* Main Activity Components ***********************************************************************/
/*                                                                                                */

public class MainActivity extends AppCompatActivity implements
        FB_Authentication_Handler,
        FirebaseAuthenticationHandler
{

    /* Member Variables *******************************************************/

    /* Authentication Model */
    private dede_FaceBookAuth facebookAuth;
    private FirebaseAuthenticationModel firebaseAuth;

    /* UI Elements */
    private TextView lbl_token;

    /* Application Lifecycle *************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Application Outlets */
        this.lbl_token = ( TextView ) findViewById( R.id.lbl_token );

        /* Authentication Initialization */
        this.firebaseAuth = new FirebaseAuthenticationModel( this );
        this.facebookAuth = new dede_FaceBookAuth( this );
        this.facebookAuth.configureFacebookLoginButton(
                (LoginButton) findViewById( R.id.fb_login_button )
        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.facebookAuth.getCallbackManager().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Facebook Authentication Handler ***************************************/

    @Override
    public void onValidAuthentication(AccessToken accessToken) {
        this.firebaseAuth.handleFacebookAccessToken( accessToken );
    }

    @Override
    public void onError(FacebookException error) {
        error = error;
    }

    /* Firebase Authentication Handler ***************************************/

    @Override
    public void handleNewUser(FirebaseUser newUser) {

        if( newUser != null ) {

            this.lbl_token.setText( newUser.getDisplayName() );

        } else {

            this.lbl_token.setText( "NULL" );

        }

    }

    @Override
    public void handleAuthenticationFailure() {

        this.lbl_token.setText( "Firebase Auth Failure" );

    }
}
