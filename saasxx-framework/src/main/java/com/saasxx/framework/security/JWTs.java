package com.saasxx.framework.security;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Jsons;

/**
 * JWT工具类，基于jose4j实现
 * 
 * @author lujijiang
 *
 */
public class JWTs {
	/**
	 * 将数据加密为jwt字符串
	 * 
	 * @param data
	 *            要加密的数据
	 * @param key
	 *            秘钥
	 * @param expirationTimeMinutesInTheFuture
	 *            过期时间，按分钟计算
	 * @return jwt字符串
	 */
	public static String encrypt(Object data, RsaJsonWebKey rsaJsonWebKey, String issuer, String audience,
			int expirationTimeMinutesInTheFuture) {
		try {
			JwtClaims claims = new JwtClaims();
			claims.setIssuer(issuer);
			claims.setAudience(audience);
			claims.setExpirationTimeMinutesInTheFuture(expirationTimeMinutesInTheFuture);
			claims.setGeneratedJwtId();
			claims.setIssuedAtToNow();
			claims.setNotBeforeMinutesInThePast(2);
			claims.setSubject("subject");
			claims.setClaim("data", Jsons.toJsonSerialized(data));
			claims.setClaim("type", data.getClass().getCanonicalName());

			JsonWebSignature jws = new JsonWebSignature();
			jws.setPayload(claims.toJson());
			jws.setKey(rsaJsonWebKey.getPrivateKey());
			jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
			jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
			String jwt = jws.getCompactSerialization();
			return jwt;
		} catch (JoseException e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 解密JWT数据
	 * 
	 * @param jwt
	 *            jwt字符串
	 * @param key
	 *            秘钥
	 * @param issuer
	 *            发行者
	 * @param audience
	 *            消费者
	 * @return 解密后的数据
	 */
	public static Object decrypt(String jwt, RsaJsonWebKey rsaJsonWebKey, String issuer, String audience) {
		try {

			JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime()
					.setAllowedClockSkewInSeconds(30).setRequireSubject().setExpectedIssuer(issuer)
					.setExpectedAudience(audience).setVerificationKey(rsaJsonWebKey.getKey()).build();
			JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
			String json = (String) jwtClaims.getClaimValue("data");
			Class<?> type = Class.forName((String) jwtClaims.getClaimValue("type"));
			return Jsons.fromJsonSerialized(json, type);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	public static void main(String[] args) throws JoseException {
		String key = "key";
		RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
		rsaJsonWebKey.setKeyId(key);
		String issuer = "issuer";
		String audience = "audience";
		String jwt = encrypt(Lang.newMap("aaaa", "bbbb"), rsaJsonWebKey, issuer, audience, 1);
		System.out.println(jwt);
		Object object = decrypt(jwt, rsaJsonWebKey, issuer, audience);
		System.out.println(object);
	}
}
