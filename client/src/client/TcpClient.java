package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpClient implements Runnable, AutoCloseable {
    private static final System.Logger log = System.getLogger("CLIENT");

    public static void main(String[] args) {
	try {
	    String name;
	    if (args.length == 0)
		name = Integer.toString(ThreadLocalRandom.current().nextInt());
	    else
		name = args[0];

	    new TcpClient(name).run();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private final SocketChannel sock;
    private final ExecutorService threadSupport;
    private final String name;
    // private final AtomicBoolean conectionClosed;

    public String getName() {
	return name;
    }

    public TcpClient(String name) throws IOException {
	this.name = name;
	this.sock = SocketChannel.open(new InetSocketAddress(25566));
	this.threadSupport = Executors.newFixedThreadPool(2);
	// this.conectionClosed = new AtomicBoolean(false);
    }

    @Override
    public void run() {
	threadSupport.execute(new WriterThread(this));
	threadSupport.execute(new ReaderThread(this));
	try {
	    threadSupport.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	} catch (InterruptedException e) {
	    log.log(System.Logger.Level.INFO, e::getMessage);
	    Thread.currentThread().interrupt();
	}
    }

    public final int sendMessage(ByteBuffer buf) throws IOException {
	return this.sock.write(buf);
    }

    public final int readMessage(ByteBuffer buf) throws IOException {
	return this.sock.read(buf);
    }

    private final void closeConnection() {
	try {
	    this.sock.close();
	    this.threadSupport.shutdown();
	    this.threadSupport.shutdownNow();
	} catch (IOException e) {
	    log.log(System.Logger.Level.INFO, e::getMessage, e);
	}
    }

    public final boolean isOpen() {
	return this.sock.isConnected();
    }

    @Override
    public void close() {
	closeConnection();
    }

}
