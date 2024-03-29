// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Scanner;

public class HashID {

    public static byte [] computeHashID(String line) throws Exception {
		if (line.endsWith("\n")) {
			// What this does and how it works is covered in a later lecture
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(line.getBytes(StandardCharsets.UTF_8));
			return md.digest();

		} else {
			line = line + "\n";
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(line.getBytes(StandardCharsets.UTF_8));
			return md.digest();
		}
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder(2 * bytes.length);
		for (byte b : bytes) {
			int unsignedByte = b & 0xFF;
			String hex = Integer.toHexString(unsignedByte);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static byte[] hexToBytes(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	public static String hexToBinary(String hexString) {
		StringBuilder binaryString = new StringBuilder();
		for (int i = 0; i < hexString.length(); i++) {
			String binary = Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
			while (binary.length() < 4) {
				binary = "0" + binary;
			}
			binaryString.append(binary);
		}
		return binaryString.toString();
	}
	public static int distance(String name1, String name2) throws Exception
	{
		return distance(HashID.computeHashID(name1), HashID.computeHashID(name2));
	}
	public static int distance(byte[] hashID1, byte[] hashID2)
	{
		int distance = 256;
		int similarBits = 0;

		String hashIDS1 = hexToBinary(bytesToHex(hashID1));
		String hashIDS2 = hexToBinary(bytesToHex(hashID2));
		                                                                                                                                   // 0101011011000000100000101100011011110011001111010110111001100100111101110110101110001100100000001111001110011011101010101001101001110011010101111100111010110001010100100100111100101010111110011011011010101111101110110001000000000110100111101010001110000110
		while (hashIDS1.charAt(similarBits) == hashIDS2.charAt(similarBits))
		{
			similarBits++;
			if (similarBits == 256)
				break;
		}

		return distance - similarBits;
	}

	//Run this file if you want to get hex of certain string, e.g: nearest request
	public static void main(String[] args) throws Exception
	{
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.print("Enter a string: ");
			String input1 = scanner.nextLine();
			String input2 = scanner.nextLine();

			String hexString = bytesToHex(computeHashID((input1 + "\n")));
			System.out.println("Hexadecimal representation: " + hexString);
			System.out.println("Binary representation: " + hexToBinary(hexString));
			System.out.println("Distance: " + distance(input1, input2));
		}
	}}
