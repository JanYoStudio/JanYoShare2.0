package pw.janyo.janyoshare.classes;

import pw.janyo.janyoshare.util.socket.SocketUtil;

public class ConnectedSocket {
    public String host;
    public String mac;
    public SocketUtil socketUtil;

    @Override
    public String toString() {
        return "ConnectedSocket{" +
                "host='" + host + '\'' +
                ", mac='" + mac + '\'' +
                ", socketUtil=" + socketUtil +
                '}';
    }
}
