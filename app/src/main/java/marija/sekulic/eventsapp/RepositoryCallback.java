package marija.sekulic.eventsapp;

// callback koji aktiviti koristi da dobije rezultat iz repository-ja
// T je tip rezultata (npr. Boolean, String, List<Event>...)
public interface RepositoryCallback<T> {
    void onResult(T result);
    void onError(String errorMessage);
}