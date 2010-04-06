package sneer.bricks.network.computers.authentication.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.util.Arrays;

import sneer.bricks.hardware.cpu.crypto.Crypto;
import sneer.bricks.hardware.cpu.crypto.Hash;
import sneer.bricks.identity.seals.Seal;
import sneer.bricks.network.computers.authentication.PublicKeyChallenges;
import sneer.bricks.pulp.network.ByteArraySocket;

class PublicKeyChallengesImpl implements PublicKeyChallenges {

	private final SecureRandom _random = new SecureRandom();
	
	@Override
	public boolean challenge(Seal contactsSeal, ByteArraySocket socket) throws IOException {
		byte[] challenge = generateChallenge();
		socket.write(challenge);
		
		PublicKey publicKey = readPublicKey(socket);
		check(contactsSeal, publicKey);
		
		byte[] challengeSignature = socket.read();
		return my(Crypto.class).verifySignature(challenge, publicKey, challengeSignature);
	}


	private void check(Seal seal, PublicKey publicKey) throws IOException {
		Hash hash = my(Crypto.class).digest(publicKey.getEncoded());
		if (!Arrays.areEqual(seal.bytes.copy(), hash.bytes.copy()))
			throw new IOException("Public Key did not match Seal.");
	}


	private PublicKey readPublicKey(ByteArraySocket socket) throws IOException {
		byte[] publicKeyBytes = socket.read();
		return decode(publicKeyBytes);
	}


	private PublicKey decode(byte[] publicKeyBytes) throws IOException {
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("ECDSA", "BC");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		
		try {
			return keyFactory.generatePublic(publicKeySpec);
		} catch (InvalidKeySpecException e) {
			throw new IOException(e);
		}
	}

	private byte[] generateChallenge() {
		byte[] result = new byte[64];
		_random.nextBytes(result);
		return result;
	}

}
