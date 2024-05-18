package httpPlay;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class NetworkCaller {
    private final String callName;
    public NetworkCaller(String callName) {
        this.callName = callName;
    }

    public String makeCall(int sec) throws Exception {

        System.out.println(callName + ": BEG call : " + Thread.currentThread());

        URI uri = new URI("https://httpbin.org/delay/" + sec);

        try(InputStream openStream = uri.toURL().openStream()) {
            return new String(openStream.readAllBytes());
        } finally {
            System.out.println(callName + ": END call : " + Thread.currentThread());
        }
    }
}
