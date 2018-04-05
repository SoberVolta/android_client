package dederides.firebaseapp.com.myapplication.data.model.event;

public interface EventModelUpdateHandler {
    void eventNameDidChange();
    void eventLocationDidChange();
    void eventOwnerDidChange();
    void eventDisabledDidChange();
    void eventQueueDidChange();
    void eventActiveRidesDidChange();
    void eventPendingDriversDidChange();
    void eventDriversDidChange();
}
