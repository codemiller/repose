package com.rackspace.papi.components.datastore.hash.remote.command;

import com.rackspace.papi.commons.util.http.HttpStatusCode;
import com.rackspace.papi.commons.util.http.ServiceClientResponse;
import com.rackspace.papi.components.datastore.common.RemoteBehavior;
import com.rackspace.papi.service.proxy.RequestProxyService;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author zinic
 */
public class Delete extends AbstractRemoteCommand {

    public Delete(String cacheObjectKey, InetSocketAddress remoteEndpoint) {
        super(cacheObjectKey, remoteEndpoint);
    }

    @Override
    public ServiceClientResponse execute(RequestProxyService proxyService, RemoteBehavior remoteBehavior) {
        return proxyService.delete(getBaseUrl(), getCacheObjectKey(), getHeaders(remoteBehavior));
    }

    @Override
    public Object handleResponse(ServiceClientResponse response) throws IOException {
        return Boolean.valueOf(response.getStatusCode() == HttpStatusCode.ACCEPTED.intValue());
    }
}
