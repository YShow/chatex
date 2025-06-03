package handlers;

import java.util.Arrays;

import com.google.protobuf.InvalidProtocolBufferException;
import globals.PacketTypes;
import iml.TextMessage;

public class MessageHandler {
    private MessageHandler() {
    }

    public static void handle(final byte[] message, final ClientHandler client) {
	try {
	    final var mess = TextMessage.parseFrom(message).toByteArray();

	    for (final var clientToSend : client.getServer().getClientsSet()) {
		if (!clientToSend.equals(client)) {
		    clientToSend.sendMessage(PacketTypes.TEXTMESSAGETYPE, mess);
		    // MessageDatabaseHandler.saveMessageToDatabase(mess.getMessage(),
		    // clientToSend);
		}
	    }

	} catch (InvalidProtocolBufferException e) {
	    client.getServer().getServerLogger().log(System.Logger.Level.INFO, e::getMessage, e);
	}
    }
}
