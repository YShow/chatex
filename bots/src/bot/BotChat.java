package bot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import globals.BufferSize;
import iml.TextMessage;

public class BotChat implements Runnable {
    private final BlockingQueue<String> messages;
    private final String name;
    private static final System.Logger log = System.getLogger("bot");
    private static final List<String> listResponse = List.of("teste1","teste2");

    public static void main(String[] args) {
	final var names = List.of("vitao", "junin", "cilao", "zieri", "zé", "junao", "teteu", "drakonam", "campos",
		"fleca");

	// final var selector = Selector.open();
	final var clientThreads = Executors.newFixedThreadPool(10);
	for (int n = 0; n < names.size(); n++) {
	    clientThreads.execute(new BotChat(Integer.toString(n)));
	}
    }

    private BotChat(String name) {
	this.name = name;
	this.messages = new SynchronousQueue<>();
    }

    @Override
    public void run() {
	try {
	    final var clientSocket = SocketChannel.open(new InetSocketAddress(25566));

	    try (clientSocket) {
		final var readthread = new Thread(() -> {
		    final var read = ByteBuffer.allocate(BufferSize.BUFFERSIZE);
		    try (clientSocket) {
			while (clientSocket.isOpen()) {
			    clientSocket.isConnected();
			    read.clear();
			    final var arrsize = clientSocket.read(read);
			    read.flip();
			    if (arrsize > -1) {
				// System.out.println(new String(read.array(), 0, arrsize) + " \n");
				// final var waittime = rand.nextInt(5, 50);
				// TimeUnit.SECONDS.sleep(waittime);
				// this.messages
				// .put(listResponse.get(rand.nextInt(listResponse.size())));
			    } else if (arrsize <= -1) {
				// assume connection is over
				log.log(System.Logger.Level.INFO, () -> "arr size " + arrsize);
				break;
			    }
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		});

		final var writeThread = new Thread(() -> {
		    final var writebuf = ByteBuffer.allocate(BufferSize.BUFFERSIZE);
		    try (clientSocket) {
			final var messageText = TextMessage.newBuilder();
			while (clientSocket.isOpen()) {
			    writebuf.clear();
			    messageText.clear();
			    final var mess = this.messages.take();
			    final var strinbuf = new StringBuilder(40).append(name).append(" : ").append(mess)
				    .toString();

			    final var messageInBytes = messageText.setMessage(strinbuf)
				    .setTimestamp(Instant.now().toEpochMilli()).build().toByteArray();

			    // writebuf.put(PacketTypes.TextMessageType.getValue()).putInt(messageInBytes.length)
			    // .put(messageInBytes).flip();
			    clientSocket.write(writebuf);
			}
		    } catch (IOException | InterruptedException e) {
			e.printStackTrace();
		    }
		});
		readthread.start();
		writeThread.start();
		readthread.join();
		writeThread.join();
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
