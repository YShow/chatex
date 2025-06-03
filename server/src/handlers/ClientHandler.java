package handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import globals.BufferSize;
import globals.ClientState;
import globals.PacketTypes;
import server.Message;
import server.TcpServer;

public final class ClientHandler implements Runnable {
    private static final System.Logger log = System.getLogger(ClientHandler.class.getName());
    private final SocketChannel client;
    private final TcpServer server;

    private String userName;
    private ClientState clientState;

    public ClientHandler(final SocketChannel soc, final TcpServer server) {
	this.client = Objects.requireNonNull(soc);
	this.server = Objects.requireNonNull(server);
	this.clientState = ClientState.ClientConnected;
    }

    @Override
    public void run() {
	try {
	    final var message = ByteBuffer.allocate(BufferSize.BUFFERSIZE);

	    this.getServer().getClientsSet().add(this);
	    while (this.client.isConnected()) {
		message.clear();
		final int read = this.client.read(message); // blocking
		if (read <= -1) {
		    log.log(System.Logger.Level.INFO, () -> "Client disconecting EOF " + read);
		    closeClient();
		    break;
		}
		message.flip();
		final var packtype = PacketTypes.getType(message.get());
		final var messagesize = message.getInt();
		final var messageInByte = getMessageInByte(message, messagesize);

		// final var newmessage = new Message(messageInByte, this, packtype);

		this.server.broadcastMessage(packtype, messageInByte, this);
		// this.broadcastToServer(newmessage); // blocking
	    }
	} catch (final IOException e) {
	    log.log(System.Logger.Level.INFO, e::getMessage, e);
	    closeClient();
	}
    }

    public final String getUserName() {
	return this.userName;
    }

    public final SocketChannel getSocket() {
	return this.client;
    }

    public final TcpServer getServer() {
	return this.server;
    }

    public final void sendMessage(final PacketTypes type, final byte[] encodedMessage) {
	Objects.requireNonNull(encodedMessage);
	Objects.requireNonNull(type);
	final var bufmess = ByteBuffer.allocate(BufferSize.BUFFERSIZE).put(type.getValue())
		.putInt(encodedMessage.length).put(encodedMessage).flip();
	sendMessageToClient(bufmess);

    }

    private final void sendMessageToClient(final ByteBuffer bufToSend) {
	try {
	    this.client.write(bufToSend);
	} catch (IOException e) {
	    log.log(System.Logger.Level.INFO, e::getMessage, e);
	    closeClient();
	}
    }

    private final byte[] getMessageInByte(final ByteBuffer message, final int messageSize) {
	if (message.hasArray()) {
	    return Arrays.copyOf(message.array(), messageSize);
	} else {
	    final var messageInByte = new byte[messageSize];
	    message.get(messageInByte);
	    return messageInByte;
	}
    }

    private final void broadcastToServer(final Message message) {
	this.server.broadcastMessage(message);
    }

    private final void closeClient() {
	this.server.closeClient(this);
    }

    @Override
    public String toString() {
	return "ClientHandler [client=" + client + ", server=" + server + ", userName=" + userName + "]";
    }
}
