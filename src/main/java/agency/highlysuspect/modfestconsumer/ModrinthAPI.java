package agency.highlysuspect.modfestconsumer;

import agency.highlysuspect.modfestconsumer.API;
import agency.highlysuspect.modfestconsumer.ModfestConsumer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.util.concurrent.RateLimiter;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ModrinthAPI extends API {
	public static final URI BASE_URI = URI.create("https://api.modrinth.com/v2/");
	
	public ModrinthAPI(HttpClient client) {
		super(client, new ModrinthRateLimiter());
	}
	
	public Version requestVersion(String versionId) throws Exception {
		System.out.println("Downloading information about version " + versionId);
		
		try {
			URI uri = BASE_URI.resolve("version/" + versionId);
			String rsp = requestAsString(uri);
			return ModfestConsumer.JSON.readValue(rsp, Version.class);
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException("when requesting version with id " + versionId));
			throw e;
		}
	}
	
	public List<Version> requestManyVersions(Collection<String> versionIds) throws Exception {
		String k = "Downloading information about versions " + String.join(", ", versionIds);
		System.out.println(k);
		
		try {
			URI uri = BASE_URI.resolve("versions?ids=" + formatAsArray0(versionIds));
			String rsp = requestAsString(uri);
			return ModfestConsumer.JSON.readValue(rsp, new TypeReference<>(){});
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException(k));
			throw e;
		}
	}
	
	public Version requestLatestVersionForProject(String projectId, Collection<String> loaders, Collection<String> gameVersions) throws Exception {
		String k = "Downloading information about the latest version for project " + projectId + " for loaders " + formatAsArray(loaders) + " and versions " + gameVersions;
		System.out.println(k);
		
		try {
			URI uri = BASE_URI.resolve("project/" + projectId + "/version?loaders=" + formatAsArray0(loaders) + "&game_versions=" + formatAsArray0(gameVersions));
			String rsp = requestAsString(uri);
			
			List<Version> candidates = ModfestConsumer.JSON.readValue(rsp, new TypeReference<>() {});
			if(candidates.size() == 0) throw new IllegalStateException("No versions are compatible?");
			
			//Sort by date published
			candidates.sort(Comparator.comparing((Version ver) -> Instant.from(DateTimeFormatter.ISO_INSTANT.parse(ver.datePublished()))).reversed());
			return candidates.get(0);
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException(k));
			throw e;
		}
	}
	
	public void downloadPrimaryFile(Path modsDir, Version version) throws Exception {
		String k = "Downloading id " + version.id() + " v" + version.number();
		System.out.println(k);
		
		try {
			File file = version.findPrimaryFile();
			String filename = file.filename();
			if(filename.contains("/..") || filename.contains("../") || filename.contains("/")) throw new IllegalArgumentException("Suss filename: " + filename);
			
			Path dest = modsDir.resolve(filename);
			if(Files.exists(dest) && Files.size(dest) > 0) {
				System.out.println("Skipping, file already exists");
				return;
			}
			
			request(file.uri(), HttpResponse.BodyHandlers.ofFile(dest));
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException(k));
			throw e;
		}
	}
	
	private String formatAsArray(Collection<String> c) {
		ArrayNode arr = new ArrayNode(JsonNodeFactory.instance);
		for(String s : c) arr.add(s);
		return arr.toString();
	}
	
	private String formatAsArray0(Collection<String> c) {
		return URLEncoder.encode(formatAsArray(c), StandardCharsets.UTF_8);
	}
	
	@SuppressWarnings("UnstableApiUsage") //guava RateLimiter
	public static class ModrinthRateLimiter implements RateLimit {
		RateLimiter limit = RateLimiter.create(5); //seconds
		
		@Override
		public void block() {
			limit.acquire();
		}
		
		//TODO, be a little more smart about this (they also send X-Ratelimit-Remaining and X-Ratelimit-Reset)
		@Override
		public void adjustRate(HttpResponse<?> r) {
			r.headers().firstValue("X-Ratelimit-Limit").map(Integer::parseInt).ifPresent(requestsPerMinute -> {
				double rps = requestsPerMinute / 60d;
				if(Math.abs(limit.getRate() - rps) > 0.01) {
					System.out.println("Modrinth requested a rate limit of " + rps + " requests per second");
				}
				limit.setRate(rps);
			});
		}
	}
	
	public static record Project(String slug, String title, @JsonProperty("versions") List<String> versionIds) {}
	public static record File(Map<String, String> hashes, @JsonProperty("url") URI uri, String filename, boolean primary) {}
	public static record Version(String id, @JsonProperty("project_id") String projectId, @JsonProperty("version_number") String number, @JsonProperty("date_published") String datePublished, List<Dependency> dependencies, List<File> files) {
		public File findPrimaryFile() {
			if(files == null || files.isEmpty()) throw new IllegalStateException("No files");
			
			for(File file : files) {
				if(file.primary()) return file;
			}
			
			//"If there isn't a primary, assume the zeroth one" ~ Emmaffle on Discord, your home for critical api docs ;)
			return files.get(0);
		}
	}
	public static record Dependency(@JsonProperty("version_id") String versionId, @JsonProperty("project_id") String projectId, @JsonProperty("dependency_type") Type dependencyType) {
		public enum Type {
			@JsonProperty("required") Required,
			@JsonProperty("optional") Optional,
			@JsonProperty("incompatible") Incompatible,
			@JsonProperty("embedded") Embedded,
			;
		}
	}
}
