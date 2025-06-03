package server;

import globals.PacketTypes;
import handlers.ClientHandler;

public record Message(byte[] buf, ClientHandler sender, PacketTypes type) {

}
