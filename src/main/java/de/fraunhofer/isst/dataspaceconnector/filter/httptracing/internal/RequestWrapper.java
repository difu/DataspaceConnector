package de.fraunhofer.isst.dataspaceconnector.filter.httptracing.internal;

import com.google.common.primitives.Bytes;

import java.io.*;
import java.util.Arrays;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Use this class to wrap incoming HTTP requests too read the message payload multiple times.
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private byte[] requestBody = new byte[0];
    private boolean isBufferFilled = false;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public byte[] getRequestBody() throws IOException {
        if (isBufferFilled) {
            return Arrays.copyOf(requestBody, requestBody.length);
        }

        var inputStream = super.getInputStream();
        if(inputStream != null){
            var buffer = new byte[128];
            var bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                requestBody = Bytes.concat(requestBody, Arrays.copyOfRange(buffer, 0, bytesRead));
            }

            isBufferFilled = true;
        }

        return requestBody;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CustomServletInputStream(getRequestBody());
    }

    private static class CustomServletInputStream extends ServletInputStream {
        private ByteArrayInputStream buffer;

        public CustomServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");
        }
    }
}