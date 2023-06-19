/**
 * @author Śliwa Adam S25853
 */

package zad1;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.FutureTask;

public class ClientTask extends FutureTask<String> implements Runnable {

    public ClientTask(Client client, List<String> requests, boolean showSendRes) {
        super(() -> {
            StringBuilder log = new StringBuilder();
            try {
                client.connect();
                client.send("login " + client.id);
                for (String req : requests) {
                    String res = client.send(req);
                    if (showSendRes) {
                        System.out.println(res);
                    }
                }
                String clog = client.send("bye and log transfer");
                log.append(clog).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return log.toString();
        });

    }

    public static ClientTask create(Client c, List<String> reqs, boolean showSendRes) {
        return new ClientTask(c, reqs, showSendRes);
    }
}
