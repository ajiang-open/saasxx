package com.saasxx.framework.security.shiro.jwt;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.jose4j.jwk.JsonWebKey.OutputControlLevel;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Jsons;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.security.shiro.ShiroRealm;
import com.saasxx.framework.security.shiro.ShiroUser;

/**
 * 专用于JWT认证的shiro realm
 * 
 * @author lujijiang
 *
 */
public class JWTShiroRealm extends ShiroRealm implements InitializingBean {

	private static Log log = Logs.getLog();

	protected RsaJsonWebKey authorizationKey;

	/**
	 * 发行者
	 */
	private String issuer = "issuer";
	/**
	 * 订阅者
	 */
	private String audience = "audience";
	/**
	 * 应用名
	 */
	private String appName;

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public boolean supports(AuthenticationToken token) {
		return token instanceof JWTAuthenticationToken;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) token;
		if (jwtAuthenticationToken.getUsername() == null || jwtAuthenticationToken.getToken() == null) {
			return null;
		}

		if ("anonymous".equals(jwtAuthenticationToken.getUsername())
				&& "anonymous".equals(jwtAuthenticationToken.getToken())) {
			return null;
		}

		JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setAllowedClockSkewInSeconds(30)
				.setRequireSubject().setExpectedIssuer(issuer).setExpectedAudience(audience)
				.setVerificationKey(authorizationKey.getKey()).build();
		try {
			String jwt = jwtAuthenticationToken.getUsername().concat(".").concat(jwtAuthenticationToken.getToken());
			JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
			String username = jwtClaims.getSubject();
			String attributes = (String) jwtClaims.getClaimValue("attributes");
			Map<String, Object> attributeMap = Jsons.fromJson(attributes, HashMap.class);
			ShiroUser shiroUser = shiroService.findUser(username, attributeMap);
			if (shiroUser != null) {
				return new SimpleAuthenticationInfo(shiroUser, token.getCredentials(), shiroUser.getUsername());
			}
			return null;
		} catch (InvalidJwtException | MalformedClaimException e) {
			log.error(e, "The token is invalid");
			return null;
		}
	}

	/**
	 * 生成JWT字符串
	 * 
	 * @param username
	 *            用户名
	 * @param attributes
	 *            用户属性
	 * @param expirationTimeMinutesInTheFuture
	 *            过期时间（分钟计算）
	 * @return JWT字符串
	 * @throws JoseException
	 */
	public String createJWT(String username, Map<String, Object> attributes, Date expirationTime) {
		try {
			attributes = new HashMap<>(attributes);
			if (expirationTime != null) {
				attributes.put("$expiration-time", expirationTime.getTime());
			} else {
				expirationTime = new Date(Long.valueOf(attributes.get("$expiration-time").toString()));
			}
			JwtClaims claims = new JwtClaims();
			claims.setIssuer(issuer);
			claims.setAudience(audience);
			claims.setExpirationTime(NumericDate.fromMilliseconds(expirationTime.getTime()));
			claims.setGeneratedJwtId();
			claims.setIssuedAtToNow();
			claims.setNotBeforeMinutesInThePast(2);
			claims.setSubject(username);
			claims.setClaim("attributes", Jsons.toJson(attributes));
			JsonWebSignature jws = new JsonWebSignature();
			jws.setPayload(claims.toJson());
			jws.setKey(authorizationKey.getPrivateKey());
			jws.setKeyIdHeaderValue(authorizationKey.getKeyId());
			jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
			String jwt = jws.getCompactSerialization();
			return jwt;
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 更新JWT字符串
	 * 
	 * @param username
	 *            用户名
	 * @param attributes
	 *            属性
	 * @return
	 */
	public String updateJWT(String username, Map<String, Object> attributes) {
		return createJWT(username, attributes, null);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(appName, "The appName should not be null");
		File dir = new File(System.getProperty("java.io.tmpdir"), appName);
		dir = new File(dir, System.getProperty("spring.profiles.active"));
		File keyFile = new File(dir, "jwt.key");
		if (keyFile.isFile()) {
			String json = FileUtils.readFileToString(keyFile, "UTF-8");
			Map<String, Object> params = Jsons.fromJson(json, Map.class);
			authorizationKey = new RsaJsonWebKey(params);
		} else {
			authorizationKey = RsaJwkGenerator.generateJwk(2048);
			authorizationKey.setKeyId(UUID.randomUUID().toString());
			String json = authorizationKey.toJson(OutputControlLevel.INCLUDE_PRIVATE);
			FileUtils.writeStringToFile(keyFile, json, "UTF-8");
		}
		super.afterPropertiesSet();
	}

}
