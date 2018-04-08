package dederides.firebaseapp.com.dederides.authentication;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/*                                                                                                */
/* Facebook Auth Components ***********************************************************************/
/*                                                                                                */

public class FacebookAuthModel implements FacebookCallback<LoginResult> {

    /* Class Constants */
    private static final String EMAIL = "email";
    private static final String PUBLIC_PROFILE = "public_profile";

    /* Member Variables ******************************************************/

    private FacebookAuthHandler m_handler;
    private CallbackManager m_callbackManager;
    private AccessToken m_accessToken = null;
    private FacebookException m_error = null;

     /* Constructor **********************************************************/

    public FacebookAuthModel( FacebookAuthHandler handler ) {

        this.m_handler = handler;
        this.m_callbackManager = CallbackManager.Factory.create();
    }

    /* Action Items **********************************************************/

    public boolean configureFacebookLoginButton(LoginButton fb_login_button) {

        /* Configure this as callback object */
        fb_login_button.setReadPermissions(
                EMAIL,
                PUBLIC_PROFILE
        );
        fb_login_button.registerCallback( this.m_callbackManager, this );
        LoginManager.getInstance().registerCallback( this.getCallbackManager(), this );

        /* Check if user already authenticated */
        this.m_accessToken = AccessToken.getCurrentAccessToken();
        if ( this.m_accessToken != null ) {
            this.m_error = null;
            return true;
        }

        return false;
    }


    /* Getters and Setters ***************************************************/

    public CallbackManager getCallbackManager() {
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
    }

    @Override
    public void onError(FacebookException error) {

        this.m_error = error;
        this.m_accessToken = null;

        m_handler.onError( this.m_error );
    }
}