package com.rackspace.papi.service.context.container;

import com.rackspace.papi.domain.Port;

import java.util.List;

public interface ContainerConfigurationService {
   List<Port> getPorts();
   
   String getVia();
   
   void setVia(String via);
   
   int getContentBodyReadLimit();
   
   void setContentBodyReadLimit(int value);
}
