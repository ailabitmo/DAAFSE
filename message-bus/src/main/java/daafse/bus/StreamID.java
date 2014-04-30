package daafse.bus;

/**
 * Created by oscii on 29/04/14.
 */
public class StreamID {
    private final String route;

    public StreamID(String route) {
        if (route == null) {
            throw new IllegalArgumentException("Route string can't be null");
        }
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreamID streamID = (StreamID) o;

        if (!route.equals(streamID.route)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }
}
