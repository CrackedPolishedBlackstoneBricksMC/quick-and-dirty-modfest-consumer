package agency.highlysuspect.modfestconsumer.modfest;

import agency.highlysuspect.modfestconsumer.API;
import agency.highlysuspect.modfestconsumer.ModfestConsumer;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

public class ModfestAPI extends API {
	public static final URI BASE_URI = URI.create("https://platform.modfest.net/");
	
	public ModfestAPI(HttpClient client) {
		super(client, new RateLimit.Unlimited());
	}
	
	public List<ModfestPlatformSubmission> requestSubmissionList() throws Exception {
		System.out.println("Downloading Modfest submissions list");
		
		String rsp = requestAsString(BASE_URI.resolve("submissions"));
		return ModfestConsumer.JSON.readValue(rsp, new TypeReference<>(){});
	}
}
