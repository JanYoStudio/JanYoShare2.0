/*
 * Created by Mystery0 on 18-2-10 下午4:00.
 * Copyright (c) 2018. All Rights reserved.
 *
 *                    =====================================================
 *                    =                                                   =
 *                    =                       _oo0oo_                     =
 *                    =                      o8888888o                    =
 *                    =                      88" . "88                    =
 *                    =                      (| -_- |)                    =
 *                    =                      0\  =  /0                    =
 *                    =                    ___/`---'\___                  =
 *                    =                  .' \\|     |# '.                 =
 *                    =                 / \\|||  :  |||# \                =
 *                    =                / _||||| -:- |||||- \              =
 *                    =               |   | \\\  -  #/ |   |              =
 *                    =               | \_|  ''\---/''  |_/ |             =
 *                    =               \  .-\__  '-'  ___/-. /             =
 *                    =             ___'. .'  /--.--\  `. .'___           =
 *                    =          ."" '<  `.___\_<|>_/___.' >' "".         =
 *                    =         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       =
 *                    =         \  \ `_.   \_ __\ /__ _/   .-` /  /       =
 *                    =     =====`-.____`.___ \_____/___.-`___.-'=====    =
 *                    =                       `=---='                     =
 *                    =     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   =
 *                    =                                                   =
 *                    =               佛祖保佑         永无BUG              =
 *                    =                                                   =
 *                    =====================================================
 *
 * Last modified 18-2-10 下午4:00
 */

package pw.janyo.janyoshare.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import vip.mystery0.logs.Logs;

public class SocketUtil {
    private static final String TAG = "SocketUtil";
    private static final int PORT = 22333;
    public static final String VERIFY_MESSAGE = "VERIFY_MESSAGE";
    private String host;
    private Socket socket = null;

    public SocketUtil(String host) {
        this.host = host;
    }

    public SocketUtil() {
    }

    public boolean connect() {
        try {
            if (socket != null)
                socket.close();
            socket = new Socket(host, PORT);
            socket.setKeepAlive(true);
            return socket.isConnected();
        } catch (IOException e) {
            Logs.wtf(TAG, "connect: ", e);
            return false;
        }
    }

    public boolean accept() {
        try {
            if (socket != null) {
                socket.close();
                Thread.sleep(200);
            }
            if (socket == null)
                socket = new ServerSocket(PORT).accept();
            socket.setKeepAlive(true);
            return socket.isConnected();
        } catch (IOException | InterruptedException e) {
            Logs.wtf(TAG, "accept: ", e);
            return false;
        }
    }

    public String receiveMessage() {
        if (!socket.isConnected())
            return "null";
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
        if (!socket.isConnected())
            return;
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
            message += '\n';
            outputStream.write(message.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Logs.wtf(TAG, "sendMessage: ", e);
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
