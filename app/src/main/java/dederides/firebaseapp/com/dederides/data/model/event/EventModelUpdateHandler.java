package dederides.firebaseapp.com.dederides.data.model.event;

public interface EventModelUpdateHandler {
    void eventNameDidChange();
    void eventLocationDidChange();
    void eventOwnerDidChange();
    void eventDisabledDidChange();
    void eventQueueDidChange();
    void eventActiveRidesDidChange();
    void eventPendingDriversDidChange();
    void eventDriversDidChange();
    void eventDeleted();
}
