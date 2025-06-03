package handlers;

//TODO: implement this class
/*
	1: send packet AUTHMESSAGETYPE
	2: client sends back atuhmessagetype with infos
	3: see if its a new user and register it or check against database for user
	4: send server info if registered / deny access and give a timeout

*/
public class AuthHandler implements Handler {
    private final byte[] message;
    private final ClientHandler clientChannel;

    public AuthHandler(final byte[] mess, final ClientHandler clientChannel) {
	this.message = mess;
	this.clientChannel = clientChannel;
    }

    @Override
    public void handle() {

    }

}