package com.rackspace.papi.components.datastore.common;

import com.rackspace.papi.commons.util.ArrayUtilities;
import com.rackspace.papi.commons.util.StringUtilities;
import com.rackspace.papi.commons.util.http.ExtendedHttpHeader;
import com.rackspace.papi.commons.util.http.HeaderConstant;
import com.rackspace.papi.commons.util.io.BufferCapacityException;
import com.rackspace.papi.commons.util.io.RawInputStreamReader;
import com.rackspace.papi.components.datastore.hash.RemoteBehavior;

import java.net.InetSocketAddress;


import javax.servlet.http.HttpServletRequest;

public class CacheRequest {

   public static final String CACHE_URI_PATH = "/powerapi/dist-datastore/objects/";
   public static final int TWO_MEGABYTES_IN_BYTES = 2097152;
   public static final HeaderConstant TTL_HEADER = ExtendedHttpHeader.X_TTL;

   private static String getCacheKey(HttpServletRequest request) {
      final String requestUri = request.getRequestURI();

      if (requestUri.startsWith(CACHE_URI_PATH) && requestUri.length() > CACHE_URI_PATH.length()) {
         final String cacheKey = requestUri.substring(CACHE_URI_PATH.length());

         if (StringUtilities.isBlank(cacheKey)) {
            throw new MalformedCacheRequestException("Cache key specified is invalid");
         }

         return cacheKey;
      }

      return null;
   }

   private static String getHostKey(HttpServletRequest request) {
      final String hostKeyHeader = request.getHeader(DatastoreHeader.HOST_KEY.toString());

      if (StringUtilities.isBlank(hostKeyHeader)) {
         throw new MalformedCacheRequestException("No host key specified in header "
                 + DatastoreHeader.HOST_KEY.toString()
                 + " - this is a required header for this operation");
      }

      return hostKeyHeader;
   }

   public static boolean isCacheRequest(HttpServletRequest request) {
      return request.getRequestURI().startsWith(CACHE_URI_PATH);
   }

   public static String urlFor(InetSocketAddress remoteEndpoint, String key) {
      return new StringBuilder("http://").append(remoteEndpoint.getAddress().getHostName()).append(":").append(remoteEndpoint.getPort()).append(CACHE_URI_PATH).append(key).toString();
   }

   public static RemoteBehavior getRequestedRemoteBehavior(HttpServletRequest request) {
      final String remoteBehaviorHeader = request.getHeader(DatastoreHeader.REMOTE_BEHAVIOR.toString());
      RemoteBehavior remoteBehavior = RemoteBehavior.ALLOW_FORWARDING;
      
      if (StringUtilities.isNotBlank(remoteBehaviorHeader)) {
         remoteBehavior = RemoteBehavior.valueOf(remoteBehaviorHeader.toUpperCase());

         if (remoteBehavior == null) {
            throw new MalformedCacheRequestException("X-PP-Datastore-Behavior header is not an expected value");
         }
      }

      return remoteBehavior;
   }

   public static CacheRequest marshallCacheRequest(HttpServletRequest request) throws MalformedCacheRequestException {
      final String cacheKey = getCacheKey(request);

      return new CacheRequest(cacheKey, getHostKey(request), -1, null, getRequestedRemoteBehavior(request));
   }

   public static CacheRequest marshallCachePutRequest(HttpServletRequest request) throws MalformedCacheRequestException {
      final String cacheKey = getCacheKey(request);
      final String hostKey = getHostKey(request);

      try {
         final String ttlHeader = request.getHeader(TTL_HEADER.toString());
         final int ttlInSeconds = StringUtilities.isBlank(ttlHeader) ? 60 : Integer.parseInt(ttlHeader);

         if (ttlInSeconds <= 0) {
            throw new MalformedCacheRequestException(TTL_HEADER.toString() + " must be a valid, positive integer number");
         }

         return new CacheRequest(cacheKey, hostKey, ttlInSeconds, RawInputStreamReader.instance().readFully(request.getInputStream(), TWO_MEGABYTES_IN_BYTES), getRequestedRemoteBehavior(request));
      } catch (NumberFormatException nfe) {
         throw new MalformedCacheRequestException(TTL_HEADER.toString() + " must be a valid, positive integer number", nfe);
      } catch (BufferCapacityException bce) {
         throw new MalformedCacheRequestException("Object is too large to store into the cache.", bce);
      } catch (Exception ex) {
         throw new MalformedCacheRequestException("Unable to parse object stream contents", ex);
      }
   }
   private final RemoteBehavior requestedRemoteBehavior;
   private final String cacheKey, hostKey;
   private final int ttlInSeconds;
   private final byte[] payload;

   public CacheRequest(String cacheKey, String hostKey, int ttlInSeconds, byte[] payload) {
      this(cacheKey, hostKey, ttlInSeconds, payload, RemoteBehavior.ALLOW_FORWARDING);
   }

   public CacheRequest(String cacheKey, String hostKey, int ttlInSeconds, byte[] payload, RemoteBehavior requestedRemoteBehavior) {
      this.cacheKey = cacheKey;
      this.hostKey = hostKey;
      this.ttlInSeconds = ttlInSeconds;
      this.payload = ArrayUtilities.nullSafeCopy(payload);
      this.requestedRemoteBehavior = requestedRemoteBehavior;
   }

   public int getTtlInSeconds() {
      return ttlInSeconds;
   }

   public boolean hasTtlSet() {
      return ttlInSeconds != -1;
   }

   public String getCacheKey() {
      return cacheKey;
   }

   public String getHostKey() {
      return hostKey;
   }

   public byte[] getPayload() {
      return payload;
   }

   public boolean hasPayload() {
      return payload != null;
   }

   public RemoteBehavior getRequestedRemoteBehavior() {
      return requestedRemoteBehavior;
   }
}