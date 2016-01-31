package org.scardiecat.styx.utils.docker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class DockerEnvironmentInspector {

    public ContainerDTO[] parse(String json) {
        Gson gson = new GsonBuilder().create();
        System.out.println("Parsing: " +json);
        return gson.fromJson(json, ContainerDTO[].class);
    }

    public Integer findPort(ContainerDTO[] containers, String instanceId, int internalAkkaPort)
    {
        System.out.println("finding : " +instanceId + " port: " + internalAkkaPort);
        for(ContainerDTO container:containers){
            if(container.Id.startsWith(instanceId)){
                for(PortBindingDTO ports:container.Ports){
                    if(ports.PrivatePort == internalAkkaPort)
                    {
                        System.out.println("Found" + ports.PublicPort);
                        return ports.PublicPort;
                    }
                }
            }
        }
        System.out.println("Nothing found");
        return null;
    }
}

