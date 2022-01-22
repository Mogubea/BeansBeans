package me.playground.voting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.crypto.BadPaddingException;

import org.bukkit.Bukkit;

public class VoteReceiver extends Thread {
	
	private final VoteManager vm;
	private final String host;
	private final int port;
	
	private ServerSocket server;
	
	private boolean enabled = true;
	
	public VoteReceiver(final VoteManager vm, String host, int port) throws Exception {
		this.vm = vm;
		this.host = host;
		this.port = port;
		
		initialize();
	}
	
	private void initialize() throws Exception {
		server = new ServerSocket();
		server.bind(new InetSocketAddress(host, port));
	}
	
	public void shutdown() {
		enabled = false;
		if (server == null) return;
		
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// Main loop.
		while (enabled) {
			try {
				Socket socket = server.accept();
				socket.setSoTimeout(5000); // Don't hang on slow connections.
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				InputStream in = socket.getInputStream();
				
				// Send them our version.
				writer.write("VOTIFIER 1.9");
				writer.newLine();
				writer.flush();
				
				// Read the 256 byte block.
				byte[] block = new byte[256];
				in.read(block, 0, block.length);
				
				// Decrypt the block.
				block = vm.getRSAManager().decrypt(block, vm.getKeyPair().getPrivate());
				int position = 0;
				
				// Perform the opcode check.
				String opcode = readString(block, position);
				position += opcode.length() + 1;
				if (!opcode.equals("VOTE")) {
					// Something went wrong in RSA.
					throw new Exception("Unable to decode RSA");
				}
				
				// Parse the block.
				String serviceName = readString(block, position);
				position += serviceName.length() + 1;
				String username = readString(block, position);
				position += username.length() + 1;
				String address = readString(block, position);
				position += address.length() + 1;
				String timeStamp = readString(block, position);
				position += timeStamp.length() + 1;
				
				final VoteService vs = vm.getService(serviceName);
				if (vs != null) {
					final Vote vote = new Vote(vs, username, address, timeStamp);
					
					// Sync the event.
					vm.getPlugin().getServer().getScheduler()
							.scheduleSyncDelayedTask(vm.getPlugin(), new Runnable() {
								public void run() {
									Bukkit.getServer().getPluginManager()
											.callEvent(new VoteEvent(vote));
								}
							});
				}
				
				// Clean up.
				writer.close();
				in.close();
				socket.close();
			} catch (SocketException e) {
				enabled = false;
				e.printStackTrace(); // Protocol error
			} catch (BadPaddingException e) {
				enabled = false;
				e.printStackTrace(); // Likely an RSA issue
			} catch (Exception e) {
				enabled = false;
				e.printStackTrace(); // General error
			}
		}
	}
	
	/**
	 * Reads a string from a block of data.
	 * 
	 * @param data
	 *            The data to read from
	 * @return The string
	 */
	private String readString(byte[] data, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < data.length; i++) {
			if (data[i] == '\n')
				break; // Delimiter reached.
			builder.append((char) data[i]);
		}
		return builder.toString();
	}
	
}
