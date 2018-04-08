package dederides.firebaseapp.com.dederides.authentication;

import com.google.firebase.auth.FirebaseUser;

public interface FirebaseAuthHandler {
    void handleNewUser(FirebaseUser newUser);

    void handleAuthenticationFailure();
}
