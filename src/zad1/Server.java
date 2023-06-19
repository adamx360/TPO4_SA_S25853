/**
 *
 *  @author Śliwa Adam S25853
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
    private final String host;
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ExecutorService executorService;
    private final Map<SocketChannel, StringBuilder> cLogs;
    private final StringBuilder serverLog;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        this.cLogs = new HashMap<>();
        this.serverLog = new StringBuilder();
    }

    public void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setReuseAddress(true);
            serverSocketChannel.bind(new InetSocketAddress(host, port));

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::runServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runServer() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        acceptClient();
                    } else if (key.isReadable()) {
                        handleClientRequest((SocketChannel) key.channel());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    private void acceptClient() throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        cLogs.put(client, new StringBuilder());
    }

    private void handleClientRequest(SocketChannel c) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(1024);
        AtomicReference<StringBuilder> mess = new AtomicReference<>(new StringBuilder());
        while (true) {
            buff.clear();
            int bRead = c.read(buff);
            if (bRead > 0) {
                buff.flip();
                String r = (new String(buff.array(), 0, bRead));
                String res = processRequest(c, r.trim());
                buff.clear();
                buff.put(res.getBytes());
                buff.flip();
                c.write(buff);
                mess.get().setLength(0);
            } else if (bRead == 0) {
                break;
            } else {
                cLogs.remove(c);
                c.close();
                break;
            }
        }
    }


    private String processRequest(SocketChannel c, String req2) {
        StringBuilder req = new StringBuilder();
        for (int i = 1; i < req2.split(" ").length; i++)
            req.append(req2.split(" ")[i]).append(" ");
        req = new StringBuilder(req.toString().trim());
        String res;
        if (req.toString().startsWith("login")) {
            String id = req.toString().split(" ")[1];
            cLogs.put(c, new StringBuilder("=== " + id + " log start ===\nlogged in\n"));
            res = "logged in";
            serverLog.append(id).append(" logged in at ").append(Time.now()).append('\n');
        } else if (req.toString().startsWith("bye")) {
            StringBuilder cLog = cLogs.get(c);
            res = cLog.toString() + "logged out\n=== " + cLog.toString().split(" ")[1] + " log end ===" + "\n";
            cLogs.remove(c);
            serverLog.append(cLog.toString().split(" ")[1]).append(" logged out at ").append(Time.now()).append('\n');
            if (req.toString().equals("bye")) {
                return "logged out";
            }
        } else {
            StringBuilder cLog = cLogs.get(c);
            serverLog.append(cLog.toString().split(" ")[1]).append(" requested at ").append(Time.now()).append(" \"").append(req).append("\"\n");
            cLog.append("Request: ").append(req).append("\n");
            res = Time.passed(req.toString().split(" ")[0], req.toString().split(" ")[1]);
            cLog.append("Result:\n").append(res);
        }
        return res;
    }

    public void stopServer() {
        try {
            if (selector != null) {
                selector.close();
            }
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (executorService != null) {
                executorService.shutdownNow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerLog() {
        return serverLog.toString();
    }
}