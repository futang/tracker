package tracker.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tracker.Events.Event;
import tracker.Events.Result;
import tracker.datatypes.Node;
import tracker.interfaces.Worker;

public class RequestHandler implements HttpAsyncRequestHandler<HttpRequest> {
    private final Worker worker;
    private static Logger logger = LogManager.getLogger();

    public RequestHandler(Worker worker) {
        this.worker = worker;
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request, HttpContext context) {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest request, HttpAsyncExchange httpexchange, HttpContext context)
            throws HttpException, IOException {
        HttpResponse response = httpexchange.getResponse();
        process(request, response, context);
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void process(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase();
        if (!method.equals("GET") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        if (request instanceof HttpEntityEnclosingRequest
                && request.getRequestLine().getUri().toLowerCase().contains("event")) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            Event event = Event.parseFrom(entity.getContent());
            Optional<Result> tr = worker.processEvt(event);
            if (tr.isPresent()) {
                Result referrer = tr.get();
                byte[] msg = referrer.toByteArray();
                NByteArrayEntity body = new NByteArrayEntity(msg);
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(body);
                logger.debug("Found referer:" + referrer);
            }
            return;
        }else if (request instanceof HttpEntityEnclosingRequest
                && request.getRequestLine().getUri().toLowerCase().contains("register")) {
            HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            HttpConnection conn = coreContext.getConnection(HttpConnection.class);
            String co = conn.toString();
            String ip= co.substring(co.indexOf(">")+1, co.lastIndexOf(":"));
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            String ports = streamToString(entity.getContent());
            String[] port = ports.split("=");
            int p = Integer.parseInt(port[1]);
            worker.addNode(new Node("http://" + ip + ":" + p + "/event", p));
            response.setStatusCode(HttpStatus.SC_OK);
            return;
        }
        // for health check
        if (method.equals("GET")) {
            response.setStatusCode(HttpStatus.SC_OK);
            return;
        }
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
    }
    
    public static String streamToString(final InputStream inputStream){
        try
        (
            final BufferedReader br
               = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            return br.lines().parallel().collect(Collectors.joining("\n"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}