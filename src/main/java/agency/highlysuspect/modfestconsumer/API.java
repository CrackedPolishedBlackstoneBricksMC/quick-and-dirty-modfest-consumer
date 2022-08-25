package agency.highlysuspect.modfestconsumer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class API {
	public API(HttpClient client, RateLimit limiter) {
		this.client = client;
		this.limiter = limiter;
	}
	
	protected final HttpClient client;
	protected final RateLimit limiter;
	
	protected HttpRequest.Builder newHttpRequestBuilder() {
		return HttpRequest.newBuilder().header("User-Agent", "quat#8515's weird modfest download project PING ME ON MODFEST DISCORD IF I FUCKED UP!!!");
	}
	
	protected <T> T request(URI uri, HttpResponse.BodyHandler<T> handler) throws IOException, InterruptedException {
		HttpRequest req = newHttpRequestBuilder().GET().uri(uri).build();
		
		limiter.block();
		HttpResponse<T> rsp = client.send(req, handler);
		limiter.adjustRate(rsp);
		
		if(rsp.statusCode() / 100 != 2) throw new RuntimeException("Got a " + rsp.statusCode() + " from " + rsp.request().uri());
		
		return rsp.body();
	}
	
	protected String requestAsString(URI uri) throws IOException, InterruptedException {
		HttpRequest req = newHttpRequestBuilder().GET().uri(uri).build();
		
		limiter.block();
		HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
		limiter.adjustRate(rsp);
		
		if(rsp.statusCode() / 100 != 2) throw new RuntimeException("Got a " + rsp.statusCode() + " from " + rsp.request().uri());
		if(rsp.body().isBlank()) throw new RuntimeException("Got a blank response from " + rsp.request().uri());
		
		return rsp.body();
	}
	
	public static interface RateLimit {
		/**
		 * Block the thread until it is okay to send a new request.
		 */
		void block();
		
		/**
		 * Adapt the rate limiter according to any directives in the response.
		 */
		void adjustRate(HttpResponse<?> resp);
		
		class Unlimited implements RateLimit {
			@Override
			public void block() {
				//Don't block
			}
			
			@Override
			public void adjustRate(HttpResponse<?> resp) {
				//Never adapt
			}
		}
	}
}
