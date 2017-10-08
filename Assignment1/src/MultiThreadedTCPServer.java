//Theodora Menelaou
//Grigoria Michail

//Libraries
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.StringTokenizer;
import java.util.Random;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

public class MultiThreadedTCPServer {

	private static class TCPWorker implements Runnable {

		private Socket client;//connection socket
		private String clientbuffer;//The message the server gets from the client
		private Integer clientbuffer_id;//The user id the server gets from the client

		public TCPWorker(Socket client) {//Initialize the clientbuffer and the clientbuffer_id
			this.client = client;
			this.clientbuffer = "";
			this.clientbuffer_id = 0;
		}

		@Override
		public void run() {

			try {
				System.out.println("Client connected with: " + this.client.getInetAddress());

				DataOutputStream output = new DataOutputStream(client.getOutputStream());//Output Stream to send data to the client
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));//Reader to read data from the client

				long start_time = System.currentTimeMillis();//Start time to mesure the server throughput
				long memory_start = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();//initial memory utilization for the current user

				ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
				long cpu_start = threadMXBean.getCurrentThreadCpuTime();//initial CPU time

				this.clientbuffer = reader.readLine();//Reads the HELLO message from the client
				this.clientbuffer_id = reader.read();//Reads the user id from the client program

				StringTokenizer st = new StringTokenizer(this.clientbuffer);
				String check = st.nextToken(" ");

				if (!check.equals("HELLO")) {//Checks if the client is actually sending to the server the HELLO message
					output.writeBytes("Wrong client message!" + System.lineSeparator());
					return;
				}

				System.out.println(
						"[" + new Date() + "] Received: " + this.clientbuffer + "User ID: " + this.clientbuffer_id);
				Random rand = new Random();
				int n = rand.nextInt(2000) + 300;//Calculates the random payload size
				n=n*1024;
				char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
				StringBuilder sb = new StringBuilder();
				Random random = new Random();
				for (int i = 1; i <= n; i++) {
				    char c = chars[random.nextInt(chars.length)];
				    sb.append(c);
				}
				String answer = sb.toString();
				answer = answer.substring(0,2);
	
				output.writeBytes(//sends to the client the response message with the user id and the payload size
						"WELCOME"+" " + this.clientbuffer_id +" " + answer+ ", Payload size: " +n+ " Kb" + System.lineSeparator());
				long end_time = System.currentTimeMillis();//End time to mesure the server throughput
				long memory_end = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();//ending memory utilization for the current user
				long cpu_end = threadMXBean.getCurrentThreadCpuTime();//ending CPU time
				long memory = memory_end - memory_start;//Memory utilization for serving the request of the current user
				long throughput = end_time - start_time;//Throughput for serving the request of the current user
				long cpu = cpu_end - cpu_start;//CPU time for serving the request of the current user

				output.writeBytes(throughput + System.lineSeparator());//Send the throughput value to the client
				output.writeBytes(memory + System.lineSeparator());//Send the memory utilization value to the client
				output.writeBytes(cpu + System.lineSeparator());//Send the CPU time value to the client

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static ExecutorService TCP_WORKER_SERVICE = Executors.newFixedThreadPool(10);

	public static void main(String args[]) {
		int port = Integer.parseInt(args[0]);//Read the port of the connection from the user in the command line
		try {
			ServerSocket socket = new ServerSocket(port);

			System.out.println("Server listening to: " + socket.getInetAddress() + ":" + socket.getLocalPort());

			while (true) {
				Socket client = socket.accept();

				TCP_WORKER_SERVICE.submit(new TCPWorker(client));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
