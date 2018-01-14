package de.maltemueller.smarthub;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import de.maltemueller.smarthub.Constants;

/**
 * Created by Daniel Mittag on 09.01.2017. Added 02.05.2017 Malte Müller.
 */

public class SocketInit {

    private static final Socket mSocket;

    private SocketInit(){
    }

    static {
        try {
            mSocket = IO.socket(Constants.SMARTHUB_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket getSocket() {
        return mSocket;
    }
}

