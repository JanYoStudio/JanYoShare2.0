package pw.janyo.janyoshare.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import vip.mystery0.tools.logs.Logs;

public class SocketUtil {
    private static final String TAG = "SocketUtil";
    private static final int port = 22333;
    private String host;
    private Socket socket = null;

    public SocketUtil(String host) {
        this.host = host;
    }

    public boolean connect() {
        try {
            if (socket != null)
                socket.close();
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            return socket.isConnected();
        } catch (IOException e) {
            Logs.wtf(TAG, "connect: ", e);
            return false;
        }
    }

    public boolean accept() {
        try {
            if (socket != null)
                socket.close();
            socket = new ServerSocket(port).accept();
            socket.setKeepAlive(true);
            return socket.isConnected();
        } catch (IOException e) {
            Logs.wtf(TAG, "accept: ", e);
            return false;
        }
    }

    public String receiveMessage() {
        StringBuilder response = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            response.append(bufferedReader.readLine());
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void sendMessage(String message) {
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
            message += '\n';
            outputStream.write(message.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Logs.wtf(TAG, "sendMessage: ", e);
        }finally {
            if (outputStream!=null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
