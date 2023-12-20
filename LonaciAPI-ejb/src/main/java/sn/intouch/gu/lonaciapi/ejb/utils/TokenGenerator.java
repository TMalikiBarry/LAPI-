package sn.intouch.gu.lonaciapi.ejb.utils;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Base64.Encoder;


/**
 * This class is for generating transaction & movement token which will replace the timestamp
 *
 */
public class TokenGenerator {
	
	public static final int RANDOM_TOKEN_LENGH = 7;
	
	public static String generateTokenOld () {
        SecureRandom randomGenerator = null;
        String token = String.valueOf(new Date().getTime());
        try {
            randomGenerator = SecureRandom.getInstance("SHA1PRNG","SUN");
            byte bytes[] = new byte[RANDOM_TOKEN_LENGH];
            randomGenerator.nextBytes(bytes);
            Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            return encoder.encodeToString(bytes) + "_" +  token;

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return token;
    }
	
	public static String generateToken () {
        SecureRandom randomGenerator = null;
        String token = String.valueOf(new Date().getTime());
        try {
            randomGenerator = SecureRandom.getInstance("SHA1PRNG","SUN");
            String random = Long.valueOf(randomGenerator.nextLong()).toString();
            return random.substring(random.length() - 1 - RANDOM_TOKEN_LENGH, random.length() - 1) + token;
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return token;
    }
	
	public static void main(String[] args) {
		System.out.println(TokenGenerator.generateToken());
	}
}
