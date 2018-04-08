package dederides.firebaseapp.com.dederides.authentication;

import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthModel implements OnCompleteListener<AuthResult> {

    private static final String TAG = "FirAuthModel";

    private FirebaseAuth m_auth;
    private FirebaseAuthHandler m_handler = null;

    public FirebaseAuthModel(FirebaseAuthHandler handler) {
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
