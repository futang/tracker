package tracker.datatypes;

public class Node {
    private final String url;
    private final int port;
    private boolean health = true;

    public Node(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

}
