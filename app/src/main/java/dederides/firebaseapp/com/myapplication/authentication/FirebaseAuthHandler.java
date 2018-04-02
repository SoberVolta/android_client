package dederides.firebaseapp.com.myapplication.authentication;

import com.google.firebase.auth.FirebaseUser;

public interface FirebaseAuthHandler {
    void handleNewUser(FirebaseUser newUser);

    void handleAuthenticationFailure();
}
