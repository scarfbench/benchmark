/*
Copyright 2017- IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package com.acmeair.securityutils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.eclipse.microprofile.jwt.JsonWebToken;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.build.Jwt;

@ApplicationScoped
public class SecurityUtils {

  @ConfigProperty(name = "secure.service.calls", defaultValue = "true")
  boolean SECURE_SERVICE_CALLS;

  /**
   * Accepts environment variable override SECURE_USER_CALLS
   */
  @ConfigProperty(name = "secure.user.calls", defaultValue = "true")
  boolean SECURE_USER_CALLS;

  private static final Logger logger = Logger.getLogger(SecurityUtils.class.getName());

  // TODO: Hardcode for now
//  private static final String secretKey = "acmeairsecret128";
  private static final String secretKey = "acmeairsecret128acmeairsecret256";

  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String SHA_256 = "SHA-256";
  private static final String UTF8 = "UTF-8";

 
  @PostConstruct
  private void initialize() {

    System.out.println("SECURE_USER_CALLS: " + SECURE_USER_CALLS);
    System.out.println("SECURE_SERVICE_CALLS: " + SECURE_SERVICE_CALLS);
  }


  public boolean secureUserCalls() {
    return SECURE_USER_CALLS;
  }

  public boolean secureServiceCalls() {
    return SECURE_SERVICE_CALLS;
  }

  /**
   * Generate simple JWT with login as the Subject.
   */
  public String generateJwt(String customerid) {
    String token = null;
    try {
      token = Jwt.claims().subject(customerid).signWithSecret(secretKey);
    } catch (Exception exception) {
      exception.printStackTrace();
    }

    return token;
  }

  /**
   * Validate simple JWT.
   */
  public boolean validateJwt(String customerid, String jwtToken) {

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("validate : customerid " + customerid);
    }

    try {
      JWTAuthContextInfo authCtx = new JWTAuthContextInfo();
      authCtx.setSignatureAlgorithm(SignatureAlgorithm.HS256);

      JsonWebToken jwt = new DefaultJWTParser(authCtx).verify(jwtToken, secretKey);
      return jwt.getSubject().equals(customerid);

    } catch (Exception exception) {
      exception.printStackTrace();
    }

    return false;
  }

  private static final ThreadLocal<MessageDigest> localSHA256 =
      new ThreadLocal<MessageDigest>() {
    @Override protected MessageDigest initialValue() {
      try {
        return MessageDigest.getInstance(SHA_256);
      } catch (NoSuchAlgorithmException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }
  };

  /**
   * Build Hash of data.
   */
  public String buildHash(String data)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = localSHA256.get();
    md.update(data.getBytes(UTF8));
    byte[] digest = md.digest();
    return Base64.getEncoder().encodeToString(digest);
  }

  private static final ThreadLocal<Mac> localMac =
      new ThreadLocal<Mac>() {
    @Override protected Mac initialValue() {
      Mac toReturn = null;
      try {
        toReturn = Mac.getInstance(HMAC_ALGORITHM);
        toReturn.init(new SecretKeySpec(secretKey.getBytes(UTF8), HMAC_ALGORITHM));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return toReturn;
    }
  };

  /**
   * Build signature of all this junk.
   */
  public String buildHmac(String method, String baseUri, String userId, String dateString, 
      String sigBody)
          throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

    List<String> toHash = new ArrayList<String>();
    toHash.add(method);
    toHash.add(baseUri);
    toHash.add(userId);
    toHash.add(dateString);
    toHash.add(sigBody);   

    Mac mac = localMac.get();
    for (String s: toHash) {
      mac.update(s.getBytes(UTF8));
    }

    return Base64.getEncoder().encodeToString(mac.doFinal());
  }

  /**
   * Build Hash of data.

	public String buildHash(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance(SHA_256);
		md.update(data.getBytes(UTF8));
		byte[] digest = md.digest();
		return Base64.getEncoder().encodeToString(digest);
	}

	/**
   * Build signature of all this junk.

  public String buildHmac(String method, String baseUri, String userId, String dateString, String sigBody)
      throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

    List<String> toHash = new ArrayList<String>();
    toHash.add(method);
    toHash.add(baseUri);
    toHash.add(userId);
    toHash.add(dateString);
    toHash.add(sigBody);

    Mac mac = null;
    try {
      mac = (Mac) macCached.clone();
    } catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    for (String s : toHash) {
      mac.update(s.getBytes(UTF8));
    }

    return Base64.getEncoder().encodeToString(mac.doFinal());
  }

  /**
   * Verify the bodyHash.
   */
  public boolean verifyBodyHash(String body, String sigBody) {

    if (sigBody.isEmpty()) {
      throw new ForbiddenException("Invalid signature (sigBody)");
    }

    if (body == null || body.length() == 0) {
      throw new ForbiddenException("Invalid signature (body)");
    }

    try {
      String bodyHash = buildHash(body);
      if (!sigBody.equals(bodyHash)) {
        throw new ForbiddenException("Invalid signature (bodyHash)");
      }
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new ForbiddenException("Unable to generate hash");
    }

    return true;
  }

  /**
   * Verify the signature junk.
   */
  public boolean verifyFullSignature(String method, String baseUri, String userId, String dateString, String sigBody,
      String signature) {

    try {
      String hmac = buildHmac(method, baseUri, userId, dateString, sigBody);

      if (!signature.equals(hmac)) {
        throw new ForbiddenException("Invalid signature (hmacCompare)");
      }
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
      throw new ForbiddenException("Invalid signature");
    }

    return true;
  }
}
