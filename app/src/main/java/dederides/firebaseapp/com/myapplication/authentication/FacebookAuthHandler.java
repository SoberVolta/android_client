package dederides.firebaseapp.com.myapplication.authentication;

import com.facebook.AccessToken;
import com.facebook.FacebookException;

public interface FacebookAuthHandler {

    /* Called when Facebook Auth Model authenticates a user */
    void onValidAuthentication(AccessToken accessToken);

    /* Called when an authentication error occurred */
    void onError(FacebookException error);

}
