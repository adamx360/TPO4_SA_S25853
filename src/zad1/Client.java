/**
 * @author Śliwa Adam S25853
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {

    public final String id;
    private final String host;
    private final int port;
    private SocketChannel channel;
    private Selector selector;
    private ByteBuffer buffer;

    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void connect() {
        try {
            this.channel = SocketChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(new InetSocketAddress(host, port));
            this.selector = Selector.open();
            this.channel.register(selector, SelectionKey.OP_CONNECT);

            while (!channel.finishConnect()) {
                selector.select();
                Iterator<SelectionKey> k = selector.selectedKeys().iterator();

                while (k.hasNext()) {
                    SelectionKey k1 = k.next();
                    k.remove();

                    if (!k1.isValid()) {
                        continue;
                    }

                    if (k1.isConnectable()) {
                        completeConnection(k1);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.buffer = ByteBuffer.allocate(1024);
    }

    public String send(String req) {
        try {
            String fReq = id + " " + req;
            buffer.clear();
            buffer.put(fReq.getBytes());
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            buffer.clear();
            int bRead;
            while (true) {
                bRead = channel.read(buffer);
                if (bRead > 0) {
                    break;
                } else if (bRead < 0) {
                    channel.close();
                    return "";
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            buffer.flip();
            return new String(buffer.array(), 0, bRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void completeConnection(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void disconnect() throws IOException {
        channel.close();
        selector.close();
    }
}