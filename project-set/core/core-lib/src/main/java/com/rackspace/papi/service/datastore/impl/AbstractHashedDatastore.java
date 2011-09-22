package com.rackspace.papi.service.datastore.impl;

import com.rackspace.papi.service.datastore.DatastoreOperationException;
import com.rackspace.papi.service.datastore.StoredElement;
import com.rackspace.papi.service.datastore.encoding.EncodingProvider;
import com.rackspace.papi.service.datastore.hash.HashProvider;
import com.rackspace.papi.service.datastore.hash.HashedDatastore;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHashedDatastore implements HashedDatastore {

    private final EncodingProvider encodingProvider;
    private final HashProvider hashProvider;
    private final String datasetPrefix;

    public AbstractHashedDatastore(String datasetPrefix, EncodingProvider encodingProvider, HashProvider hashProvider) {
        this.encodingProvider = encodingProvider;
        this.hashProvider = hashProvider;
        this.datasetPrefix = datasetPrefix;
    }

    public String getDatasetPrefix() {
        return datasetPrefix;
    }

    public EncodingProvider getEncodingProvider() {
        return encodingProvider;
    }

    public HashProvider getHashProvider() {
        return hashProvider;
    }

    private byte[] getHash(String key) {
        return hashProvider.hash(datasetPrefix + key);
    }

    @Override
    public StoredElement get(String key) throws DatastoreOperationException {
        final byte[] keyHash = getHash(key);

        return get(encodingProvider.encode(keyHash), keyHash);
    }

    @Override
    public void put(String key, byte[] value) throws DatastoreOperationException {
        put(key, value, 5, TimeUnit.MINUTES);
    }

    @Override
    public void put(String key, byte[] value, int ttl, TimeUnit timeUnit) throws DatastoreOperationException {
        final byte[] keyHash = getHash(key);

        put(encodingProvider.encode(keyHash), keyHash, value, ttl, timeUnit);
    }

    @Override
    public StoredElement getByHash(String encodedHashString) throws DatastoreOperationException {
        return get(encodedHashString, encodingProvider.decode(encodedHashString));
    }

    @Override
    public void putByHash(String encodedHashString, byte[] value) {
        put(encodedHashString, encodingProvider.decode(encodedHashString), value, 3, TimeUnit.MINUTES);
    }

    @Override
    public void putByHash(String encodedHashString, byte[] value, int ttl, TimeUnit timeUnit) throws DatastoreOperationException {
        put(encodedHashString, encodingProvider.decode(encodedHashString), value, ttl, timeUnit);
    }

    protected abstract StoredElement get(String name, byte[] id) throws DatastoreOperationException;

    protected abstract void put(String name, byte[] id, byte[] value, int ttl, TimeUnit timeUnit) throws DatastoreOperationException;
}