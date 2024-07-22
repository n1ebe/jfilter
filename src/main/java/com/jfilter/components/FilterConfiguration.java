package com.jfilter.components;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfilter.converter.SerializationConfig;
import com.jfilter.mapper.FilterObjectMapper;
import com.jfilter.mapper.FilterXmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static com.jfilter.FilterConstantsHelper.MEDIA_SUB_TYPE_JSON;
import static com.jfilter.FilterConstantsHelper.MEDIA_SUB_TYPE_JSON2;
import static com.jfilter.FilterConstantsHelper.MEDIA_SUB_TYPE_XML;
import static com.jfilter.FilterConstantsHelper.MEDIA_SUB_TYPE_XML2;
import static com.jfilter.FilterConstantsHelper.MEDIA_TYPE_APPLICATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

/**
 * This class gives access to extending of ObjectMapper lists and control of filter functionality
 */
@Component
public class FilterConfiguration {
    private boolean enabled;
    private ConcurrentMap<MediaType, ObjectMapper> mapperList;
    private ObjectMapperCache objectMapperCache;
    private SerializationConfig serializationConfig;
    private boolean useDefaultConverters;
    private List<HttpMessageConverter<Object>> customConverters;

    public FilterConfiguration() {
        mapperList = new ConcurrentHashMap<>();
        customConverters = new ArrayList<>();
        serializationConfig = new SerializationConfig();
        configureDefaultMappers();
    }

    private void configureDefaultMappers() {
        // Default JSON mappers
        setMapper(APPLICATION_JSON, Jackson2ObjectMapperBuilder.json().build());
        setMapper(new MediaType(MEDIA_TYPE_APPLICATION, MEDIA_SUB_TYPE_JSON, Charset.defaultCharset()), Jackson2ObjectMapperBuilder.json().build());
        setMapper(new MediaType(MEDIA_TYPE_APPLICATION, MEDIA_SUB_TYPE_JSON2), Jackson2ObjectMapperBuilder.json().build());

        // Default XML mappers
        setMapper(APPLICATION_XML, Jackson2ObjectMapperBuilder.xml().build());
        setMapper(new MediaType(MEDIA_TYPE_APPLICATION, MEDIA_SUB_TYPE_XML, Charset.defaultCharset()), Jackson2ObjectMapperBuilder.xml().build());
        setMapper(new MediaType(MEDIA_TYPE_APPLICATION, MEDIA_SUB_TYPE_XML2), Jackson2ObjectMapperBuilder.xml().build());
        setMapper(new MediaType(MEDIA_TYPE_APPLICATION, MEDIA_SUB_TYPE_XML), Jackson2ObjectMapperBuilder.xml().build());
    }

    @Autowired
    @SuppressWarnings("unused")
    public FilterConfiguration setObjectMapperCache(@Lazy ObjectMapperCache objectMapperCache) {
        this.objectMapperCache = objectMapperCache;
        return this;
    }

    @Autowired
    @SuppressWarnings("unused")
    private FilterConfiguration setWebApplicationContext(WebApplicationContext webApplicationContext) {
        enabled = FilterProvider.isFilterEnabled(webApplicationContext);
        return this;
    }

    protected ObjectMapper getMapper(MediaType mediaType) {
        return mapperList.get(mediaType);
    }

    public void setMapper(MediaType mediaType, ObjectMapper objectMapper) {
        mapperList.put(mediaType, objectMapper);
    }

    protected List<MediaType> supportedMediaTypes() {
        return new ArrayList<>(mapperList.keySet());
    }

    public ObjectMapperCache getObjectMapperCache() {
        return objectMapperCache;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public FilterConfiguration setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public SerializationConfig getSerializationConfig() {
        return serializationConfig;
    }

    public boolean isUseDefaultConverters() {
        return useDefaultConverters;
    }

    public FilterConfiguration setUseDefaultConverters(boolean useDefaultConverters) {
        this.useDefaultConverters = useDefaultConverters;
        return this;
    }

    public <T extends AbstractJackson2HttpMessageConverter> FilterConfiguration withCustomConverter(T converter) {
        if (converter == null)
            throw new IllegalArgumentException("Converter couldn't be null");

        if (!(converter.getObjectMapper() instanceof FilterObjectMapper) &&
                !(converter.getObjectMapper() instanceof FilterXmlMapper))
            throw new IllegalArgumentException("Converter should contain FilterObjectMapper or FilterXmlMapper in objectMapper property for correct filtering");
        customConverters.add(converter);
        return this;
    }

    public List<HttpMessageConverter<Object>> getCustomConverters() {
        return customConverters;
    }

    public void findObjectMapper(MediaType mediaType, Consumer<ObjectMapper> onFind) {
        if (mediaType == null || onFind == null)
            throw new IllegalArgumentException("mediaType or onFind operation can't be null");

        ObjectMapper objectMapper = getMapper(mediaType);
        if (objectMapper != null)
            onFind.accept(objectMapper);
    }

    public void findObjectMapper(Consumer<ObjectMapper> onFind) {
        if (onFind != null) {
            mapperList.forEach((mediaType, objectMapper) -> onFind.accept(objectMapper));
        } else
            throw new IllegalArgumentException("onFind operation can't be null");
    }
}
