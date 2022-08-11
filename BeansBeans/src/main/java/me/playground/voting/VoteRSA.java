package me.playground.voting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * Static RSA utility methods for encrypting and decrypting blocks of
 * information.
 * 
 * @author Blake Beaupain
 */
public class VoteRSA {
	
	/**
	 * Encrypts a block of data.
	 * 
	 * @param data
	 *            The data to encrypt
	 * @param key
	 *            The key to encrypt with
	 * @return The encrypted data
	 * @throws Exception
	 *             If an error occurs
	 */
	public byte[] encrypt(byte[] data, PublicKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	/**
	 * Decrypts a block of data.
	 * 
	 * @param data
	 *            The data to decrypt
	 * @param key
	 *            The key to decrypt with
	 * @return The decrypted data
	 * @throws Exception
	 *             If an error occurs
	 */
	public byte[] decrypt(byte[] data, PrivateKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}
	
	/**
	 * Generates an RSA key pair.
	 * 
	 * @param bits
	 *            The amount of bits
	 * @return The key pair
	 */
	public KeyPair generate(int bits) throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits,
				RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		return keygen.generateKeyPair();
	}
	
	/**
	 * Saves the key pair to the disk.
	 * 
	 * @param directory
	 *            The directory to save to
	 * @param keyPair
	 *            The key pair to save
	 * @throws Exception
	 *             If an error occurs
	 */
	public void save(File directory, KeyPair keyPair) throws Exception {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();

		// Store the public key.
		X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKey.getEncoded());
		FileOutputStream out = new FileOutputStream(directory + "/public.key");
		
		out.write(Base64.getEncoder().encode(publicSpec.getEncoded()));
		out.close();

		// Store the private key.
		PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		out = new FileOutputStream(directory + "/private.key");
		
		out.write(Base64.getEncoder().encode(privateSpec.getEncoded()));
		out.close();
	}

	/**
	 * Loads an RSA key pair from a directory. The directory must have the files
	 * "public.key" and "private.key".
	 * 
	 * @param directory
	 *            The directory to load from
	 * @return The key pair
	 * @throws Exception
	 *             If an error occurs
	 */
	public KeyPair load(File directory) throws Exception {
		// Read the public key file.
		File publicKeyFile = new File(directory + "/public.key");
		FileInputStream in = new FileInputStream(directory + "/public.key");
		byte[] encodedPublicKey = new byte[(int) publicKeyFile.length()];
		in.read(encodedPublicKey);
		encodedPublicKey = Base64.getDecoder().decode(new String(encodedPublicKey));
		in.close();

		// Read the private key file.
		File privateKeyFile = new File(directory + "/private.key");
		in = new FileInputStream(directory + "/private.key");
		byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
		in.read(encodedPrivateKey);
		encodedPrivateKey = Base64.getDecoder().decode(new String(encodedPrivateKey));
		in.close();

		// Instantiate and return the key pair.
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return new KeyPair(publicKey, privateKey);
	}
	
	public String readString(byte[] data, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < data.length; i++) {
			if (data[i] == '\n')
				break; // Delimiter reached.
			builder.append((char) data[i]);
		}
		return builder.toString();
	}

}
