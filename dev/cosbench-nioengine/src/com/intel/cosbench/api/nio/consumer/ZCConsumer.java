package com.intel.cosbench.api.nio.consumer;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * One zero-copy consumer, which can accept different consumer sink.
 * 
 * @author ywang19
 *
 * @param <T>
 */
public class ZCConsumer<T> extends AbstractAsyncResponseConsumer<HttpResponse> {

    private final ConsumerSink<T> sink;
    private HttpResponse response;
    
    public ZCConsumer(final ConsumerSink<T> sink) {
        super();
        
        this.sink = sink;
    }

    @Override
    protected void onResponseReceived(final HttpResponse response) {
        this.response = response;
    }

    
    @Override
    protected void onEntityEnclosed(
            final HttpEntity entity, final ContentType contentType) throws IOException {
        this.sink.connect(contentType);
    }

    @Override
    protected void onContentReceived(
            final ContentDecoder decoder, final IOControl ioctrl) throws IOException {
        this.sink.consume(decoder);
        
        if (decoder.isCompleted()) {
            this.sink.disconnect();
        }
    }
	
    @Override
    protected HttpResponse buildResult(final HttpContext context) throws Exception {
//	    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//	        throw new HttpException("Request failed: " + response.getStatusLine());
//	    }
        
        return this.response;
    }
    
    @Override
    public HttpResponse getResult() {
    	Header header = response.getFirstHeader(HTTP.CONTENT_LEN);
    	if(header == null)
    		response.addHeader(HTTP.CONTENT_LEN, String.valueOf(sink.getLength()));
    	
    	return response;
    }

    @Override
    protected void releaseResources() {
    	this.sink.close();
    }

}

