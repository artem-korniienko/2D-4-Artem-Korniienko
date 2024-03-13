// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashID {

    public static byte [] computeHashID(String line) throws Exception {
		if (line.endsWith("\n")) {
			// What this does and how it works is covered in a later lecture
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(line.getBytes(StandardCharsets.UTF_8));
			return md.digest();

		} else {
			// 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
			throw new Exception("No new line at the end of input to HashID");
		}
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder(2 * bytes.length);
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
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

	public static int distance(byte[] hashID1, byte[] hashID2)
	{
		int distance = 256;
		int similarBits = 0;

		String hashIDS1 = hexToBinary(bytesToHex(hashID1));
		String hashIDS2 = hexToBinary(bytesToHex(hashID2));

		while (hashIDS1.charAt(similarBits) == hashIDS2.charAt(similarBits))
		{
			similarBits++;
			if (similarBits == 256)
				break;
		}

		return distance - similarBits;
	}

	public static void main(String[] args) throws Exception
	{
		byte[] hashID = computeHashID("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n");
		for (byte b : hashID)
		{
			System.out.println(b);
		}
		System.out.println(bytesToHex(computeHashID("Hello World!\n")));
		System.out.println(hexToBinary(bytesToHex(hashID)));
		System.out.println(distance(computeHashID("ananus\n"), computeHashID("ananus\n")));
	}}
