//Theodora Menelaou
//Grigoria Michail

//Libraries
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.Socket;
import java.util.Date;


public class TCPClient {
	public static void main(String args[]) {

		double avglatency[] = new double[10];// Average Latency array(10 users)
		double throughput[] = new double[10];//Throughput array(10 users)
		double memory_data[] = new double[10];// Average Memory utilization array(10 users)
		double cpu_data[] = new double[10]; //Average CPU Load(10 users)

		double RTTsum = 0;//Set the summation of RTT times to zero

		try {
		
			String message, response;//message: the message the client sends to the server
									//response: the message the server sends back to the client
			Double time = 0.0; //Set initial throughput to zero
			Double memory = 0.0; //Set initial memory utilization to zero
			Double cpu = 0.0; //Set initial CPU to zero
			
			int port = Integer.parseInt(args[1]);//Read the port number from the command line

			Socket socket = new Socket(args[0], port);//Connection setup
			
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());//Output stream to send the message to the server
			BufferedReader server = new BufferedReader(new InputStreamReader(socket.getInputStream()));//Reader to read the server's reply

			int k = 0;//Set user-id number to zero(initialize it)
			int i = 1;//Initialize the number of requests to 1
			
			for (int j = 0; j <10 ; j++) {//10 users repetition
				k = j + 1; //Simulated user-id
				while (i <= 300) {//300 requests per user repetition

					message = "HELLO" + " " + socket + System.lineSeparator();//message to be sent to the server

					long start_time = System.currentTimeMillis();//RTT beginning value

					output.writeBytes(message);//Send the message to the server
					output.write(k);//Send the server the user-id number
					response = server.readLine();//Read from the server its reply

					System.out.println("[" + new Date() + "] Received: " + response);

					long end_time = System.currentTimeMillis(); //RTT ending value

					long RTT = end_time - start_time;//RTT(Communication Latency) the i request of id user
					RTTsum = RTTsum + RTT;//RTT summation for the 300 requests of id user
					time = time + (double) server.read();//Throughput of the current user
					memory = memory + (double) server.read();//Memory utilization for the current user
					cpu = cpu + (double) server.read();//CPU Load for the current user

					socket.close();//Close of the connection
					
					if (i != 300) {//If there are still requests to execute we start new connection with the server
						socket = new Socket(args[0], port); // dimiourgia
						output = new DataOutputStream(socket.getOutputStream());
						server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					}
					
					i++;//go to the next request of the client

				} //End while
				
				throughput[j] = 300 / time;//Calculate the average throughput for the user id
				avglatency[j] = RTTsum / 300;//Calculate the average latency for the user id
				memory_data[j] = memory / 300;//Calculate the average memory utilization for the user id
				cpu_data[j] = cpu / 300; //Calculate the average CPU Load for the user id

				RTTsum = 0;//Set the RTT sum to zero for the next user

				if (j < 9) {//If there are still users to be served we start new connection with the server
					socket = new Socket(args[0], port); // dimiourgia
					output = new DataOutputStream(socket.getOutputStream());
					server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				}
				
				i = 1;//set the counter for the requests to one
			}//End for
			
			socket.close();//Close of the connection

			System.out.println();//new line
			System.out.println("Latency");
			for (int kk = 0; kk < 10; kk++) {//Print the average latency array
				System.out.println(avglatency[kk]);
			}
			System.out.println();//new line

			System.out.println("Throughput");//Print the throughput array
			for (int kk = 0; kk < 10; kk++) {

				System.out.println(throughput[kk]);
			}
			System.out.println();//new line
			System.out.println("Average CPU Load");//Print the average CPU Load array
			for (int kk = 0; kk < 10; kk++) {
				System.out.println(cpu_data[kk]);
			}
			System.out.println();//new line
			
			System.out.println("Average Memory Utilization");//Print the average memory utilization array
			for (int kk = 0; kk < 10; kk++) {
				System.out.println(memory_data[kk]);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}