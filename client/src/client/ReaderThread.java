package client;

import java.io.IOException;
import java.nio.ByteBuffer;

import globals.BufferSize;
import globals.PacketTypes;
import iml.TextMessage;

class ReaderThread implements Runnable {
    private static final System.Logger log = System.getLogger("READER THREAD");
    private final ByteBuffer mes;
    private final TcpClient cli;

    public ReaderThread(TcpClient cli) {
	this.mes = ByteBuffer.allocate(BufferSize.BUFFERSIZE);
	this.cli = cli;
    }

    @Override
    public void run() {
	startReading();
    }

    private void startReading() {
	try (this.cli) {
	    while (this.cli.isOpen()) {
		this.mes.clear();
		final int arrsize = this.cli.readMessage(this.mes);
		this.mes.flip();
		if (arrsize > -1) {
		    final var messageType = PacketTypes.getType(this.mes.get());
		    final var messagesize = this.mes.getInt();
		    if (messageType == PacketTypes.TEXTMESSAGETYPE) {
			final var messageinByte = copyBufferToBytes(messagesize);
			final var messageText = TextMessage.parseFrom(messageinByte);
			System.out.println(messageText.getMessage());
		    }

		} else if (arrsize <= -1) {
		    log.log(System.Logger.Level.INFO, "Client disconected EOF");
		    break;
		}
	    }
	} catch (IOException e) {
	    // only log in debug cause if connection closes it will throw a exception every
	    // time
	    log.log(System.Logger.Level.INFO, e::getMessage, e);
	}

    }

    private final byte[] copyBufferToBytes(int size) {
	final var messagebyte = new byte[size];
	this.mes.get(messagebyte);
	return messagebyte;
    }

}