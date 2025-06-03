package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Scanner;

import globals.BufferSize;
import globals.PacketTypes;
import iml.TextMessage;

class WriterThread implements Runnable {
    private final Scanner reader = new Scanner(System.in);
    private final ByteBuffer mes;
    private final TcpClient cli;
    private static final System.Logger log = System.getLogger("WRITER THREAD");

    public WriterThread(TcpClient cli) {
	this.cli = cli;
	this.mes = ByteBuffer.allocate(BufferSize.BUFFERSIZE);
    }

    @Override
    public void run() {
	startReading();
    }

    private void startReading() {
	final var quitcom = this.cli.getName() + ": " + "!sair";
	try (this.cli) {

	    while (this.cli.isOpen()) {
		this.mes.clear();

		final var message = new StringBuilder(this.cli.getName().length() + 30).append(this.cli.getName())
			.append(": ").append(reader.nextLine()).toString();
		final var textmessage = TextMessage.newBuilder().setMessage(message)
			.setTimestamp(Instant.now().toEpochMilli()).build().toByteArray();

		this.mes.put(PacketTypes.TEXTMESSAGETYPE.getValue()).putInt(textmessage.length).put(textmessage).flip();
		this.cli.sendMessage(mes);

		if (quitcom.equals(message)) {
		    reader.close();
		    break;
		}
	    }
	} catch (IOException e) {
	    // only log in debug cause if connection closes it will throw a exception every
	    // time
	    log.log(System.Logger.Level.DEBUG, e::getMessage, e);
	}

    }
}