package com.jfilter.components;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfilter.converter.MethodParameterDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ObjectMapper cache
 * <p>
 * This class contains cached list of ObjectMappers which used in {@link FilterConverter#write(Object, MediaType, HttpOutputMessage)}
 */
@Component
public class ObjectMapperCache {
    private ConcurrentMap<MethodParameterDetails, ObjectMapper> items;
    private FilterConfiguration filterConfiguration;

    public ObjectMapperCache() {
        items = new ConcurrentHashMap<>();
    }

    @Autowired
    public ObjectMapperCache setFilterConfiguration(@Lazy FilterConfiguration filterConfiguration) {
        this.filterConfiguration = filterConfiguration;
        return this;
    }

    public ObjectMapper findObjectMapper(MethodParameterDetails item) {
        ObjectMapper objectMapper = items.get(item);
        if (objectMapper == null)
            objectMapper = addNewMapper(item);
        return objectMapper;
    }

    private ObjectMapper addNewMapper(MethodParameterDetails item) {
        ObjectMapper configuredObjectMapper = filterConfiguration.getMapper(item.getMediaType()).copy();
        ObjectMapper objectMapper = new FilterObjectMapperBuilder(configuredObjectMapper)
                .withFilterFields(item.getFilterFields())
                .withSetSerializationConfig(filterConfiguration.getSerializationConfig())
                .build();
        items.put(item, objectMapper);
        return objectMapper;
    }
}
