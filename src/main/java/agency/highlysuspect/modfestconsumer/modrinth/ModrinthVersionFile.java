package agency.highlysuspect.modfestconsumer.modrinth;

import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.Map;

public class ModrinthVersionFile {
	Map<String, String> hashes;
	@SerializedName("url") URI uri;
	String filename;
	boolean primary;
	
	@Override
	public String toString() {
		return "ModrinthVersionFile{hashes=%s, uri=%s, filename='%s', primary=%s}".formatted(hashes, uri, filename, primary);
	}
}
