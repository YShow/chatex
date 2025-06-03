package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import globals.PacketTypes;
import handlers.ClientHandler;
import handlers.MessageHandler;

public final class TcpServer implements Runnable {
    private static final System.Logger log = System.getLogger(TcpServer.class.getName());

    private final int port;
    private final List<SocketChannel> conectionList;

    private final Set<ClientHandler> clientSet;
    private final ExecutorService threadPool;

    public static void main(final String[] args) {
	System.setProperty("hsqldb.reconfig_logging", "false");
	new TcpServer(25566).run();
    }

    public TcpServer(final int port) {
	this.port = Objects.requireNonNullElse(port, 25566);
	this.conectionList = new CopyOnWriteArrayList<>();
	this.threadPool = Executors.newFixedThreadPool(Integer.getInteger("chatex.maxclientsthread", 50));
	this.clientSet = ConcurrentHashMap.<ClientHandler>newKeySet();
    }

    public final Set<ClientHandler> getClientsSet() {
	return this.clientSet;
    }

    @Override
    public void run() {
	log.log(System.Logger.Level.INFO, () -> "SERVER STARTED");

	try (final var server = ServerSocketChannel.open().bind(new InetSocketAddress(this.port))) {
	    while (server.isOpen()) {
		handleConnections(server);
	    }
	} catch (final IOException e) {
	    log.log(System.Logger.Level.INFO, e::getMessage, e);
	    closeAllClients();
	}
	this.threadPool.shutdown();
	this.threadPool.shutdownNow();

	log.log(System.Logger.Level.INFO, () -> "SERVER SHUTDOWN");
    }

    private final void handleConnections(final ServerSocketChannel server) throws IOException {
	final var soc = server.accept();
	if (!this.conectionList.contains(soc)) {
	    this.conectionList.add(soc);
	    this.threadPool.execute(new ClientHandler(soc, this));
	    log.log(System.Logger.Level.INFO, () -> "Client connected: " + soc.toString());
	}
    }

    public final void broadcastMessage(final Message message) {
	switch (message.type()) {
	case AUTHMESSAGETYPE -> throw new UnsupportedOperationException();
	case TEXTMESSAGETYPE -> MessageHandler.handle(message.buf(), message.sender());
	case REJECTTYPE -> throw new UnsupportedOperationException();

	default -> throw new IllegalArgumentException("Unexpected value: " + message.type());
	}
    }

    public final void broadcastMessage(final PacketTypes type, final byte[] messageInByte, final ClientHandler sender) {
	switch (type) {
	case AUTHMESSAGETYPE -> throw new UnsupportedOperationException();
	case TEXTMESSAGETYPE -> MessageHandler.handle(messageInByte, sender);
	case REJECTTYPE -> throw new UnsupportedOperationException();

	default -> throw new IllegalArgumentException("Unexpected value: " + type);
	}
    }

    public final List<SocketChannel> getUserList() {
	return this.conectionList;
    }

    public final TcpServer getServer() {
	return this;
    }

    public static final System.Logger getServerLogger() {
	return log;
    }

    private final void closeAllClients() {
	log.log(System.Logger.Level.INFO, () -> "Disconnecting all clients");
	this.clientSet.forEach(this::closeClient);
    }

    public final void closeClient(final ClientHandler client) {
	log.log(System.Logger.Level.INFO, () -> "Client disconected: " + client.toString());
	try {
	    client.getSocket().close();
	} catch (final IOException e) {
	    log.log(System.Logger.Level.DEBUG, e::getMessage, e);
	}
	this.conectionList.remove(client.getSocket());
	this.clientSet.remove(client);
    }

}
