package agency.highlysuspect.modfestconsumer;

import agency.highlysuspect.modfestconsumer.modfest.ModfestPlatformSubmission;
import agency.highlysuspect.modfestconsumer.modrinth.ModrinthDependency;
import agency.highlysuspect.modfestconsumer.modrinth.ModrinthVersion;
import agency.highlysuspect.modfestconsumer.modrinth.ModrinthVersionSet;
import agency.highlysuspect.modfestconsumer.modfest.ModfestAPI;
import agency.highlysuspect.modfestconsumer.modrinth.ModrinthAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModfestConsumer {
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	
	public static void main(String[] args) throws Exception {
		HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
		ModfestAPI modfestApi = new ModfestAPI(client);
		ModrinthAPI modrinthApi = new ModrinthAPI(client);
		
		//Step 0: Load locally cached data about Modrinth versions. They're not immutable, but modders changing them (as opposed to uploading a new version) is rare
		ModrinthVersionSet.Cache versionCache = ModrinthVersionSet.Cache.load(Paths.get("./modrinth_version_cache.json"));
		System.out.println("Loaded " + versionCache.size() + " Modrinth versions from the local cache");
		
		//Step 1: Download the submissions from Modfest Platform. This isn't cached.
		List<ModfestPlatformSubmission> submissions = modfestApi.requestSubmissionList();
		System.out.println("Modfest has " + submissions.size() + " submissions: " + ModfestPlatformSubmission.bigConcat(submissions));
		
		//Step 2: Learn about each Modrinth version corresponding to the Modfest submissions.
		ModrinthVersionSet submissionVersions = new ModrinthVersionSet();
		submissions.forEach(sub -> submissionVersions.put(versionCache.getVersionOrDownload(modrinthApi, sub.modrinthVersionId)));
		versionCache.save();
		
		//Step 3: Learn about all required dependencies, and dependencies's dependencies, etc, until the complete set of transitive dependencies is learned.
		//Solve the diamond problem by picking the newer version of any mutual dependencies... as they are encountered when iterating. I don't think this is actually sound.
		ModrinthVersionSet submissionsIncludingTransitiveDeps = submissionVersions.copy();
		Map<String, ModrinthVersion> projectIdToLatestVersion = new HashMap<>();
		
		//Resolving dep bugs i found while bisecting
		submissionsIncludingTransitiveDeps.put(versionCache.getVersionOrDownload(modrinthApi, "Yp8wLY1P")); //Sodium
		
		int lastSize;
		do {
			lastSize = submissionsIncludingTransitiveDeps.size();
			
			for(ModrinthVersion ver : submissionsIncludingTransitiveDeps.allVersions()) {
				if(ver.dependencies == null) continue;
				for(ModrinthDependency dep : ver.dependencies) {
					if(dep.dependencyType != ModrinthDependency.Type.Required) continue;
					
					String versionId;
					if(dep.versionId != null) {
						versionId = dep.versionId;
					} else {
						if(dep.projectId == null) {
							System.err.println("Skipping dependency with neither project ID nor version ID, weird");
							continue;
						}
						//Modrinth can sometimes have files with deps with no version ID, but a project ID. Take this to mean the latest available version for this project.
						//THis information cooouuuld be cached but it kind of shouldn't be, what if a dep updates.
						if(projectIdToLatestVersion.containsKey(dep.projectId)) {
							versionId = projectIdToLatestVersion.get(dep.projectId).id;
						} else {
							ModrinthVersion latestVer = modrinthApi.requestLatestVersionForProject(dep.projectId, List.of("quilt", "fabric"), List.of("1.19.2", "1.19.1", "1.19"));
							versionCache.put(latestVer);
							projectIdToLatestVersion.put(dep.projectId, latestVer);
							versionId = latestVer.id;
						}
					}
					ModrinthVersion newDep = versionCache.getVersionOrDownload(modrinthApi, versionId);
					if(newDep == null) continue;
					
					//(Awesome hacks) Ban fabric-language-kotlin and fabric-api
					if(Set.of("Ha28R6CL", "P7dR8mSH").contains(newDep.projectId)) {
						System.out.println(ver.id + " declares a dep on fabric-language-kotlin or fabric-api uhhh No sorry");
						continue;
					}
					
					ModrinthVersion existingDep = submissionsIncludingTransitiveDeps.getExistingVersionForProject(newDep.projectId);
					if(existingDep != null && !Objects.equals(newDep.number, existingDep.number)) {
						//The set of deps already includes a version for the given project. They must fight to the death
						System.err.printf("Resolving conflict between versions new %s (%s) and old %s (%s) for project id %s%n", newDep.id, newDep.number, existingDep.id, existingDep.number, newDep.projectId);
						long goodParseOld = goodSemVerParserYeah(existingDep.number);
						long goodParseNew = goodSemVerParserYeah(newDep.number);
						if(goodParseOld > goodParseNew) {
							System.err.println("old > new");
							continue;
						}
						System.err.println("old <= new");
					}
					
					submissionsIncludingTransitiveDeps.put(newDep);
				}
			}
		} while(submissionsIncludingTransitiveDeps.size() != lastSize);
		System.out.println("Modpack has " + lastSize + " versions to download");
		
		//Step 4: Download each version
		Path modsDir = Paths.get("./mods");
		Files.createDirectories(modsDir);
		
//		Set<String> acceptableFilenames = submissionsIncludingTransitiveDeps.allVersions().stream()
//			.map(v -> v.findPrimaryFile().filename)
//			.collect(Collectors.toSet());
//		for(Path p : Files.walk(modsDir, 1).toList()) {
//			if(!Files.isRegularFile(p)) continue;
//			if(!acceptableFilenames.contains(p.getFileName().toString())) {
//				System.err.println("Deleting " + p + " because it's not part of the modpack");
//				Files.delete(p);
//			}
//		}
		
		for(ModrinthVersion ver : submissionsIncludingTransitiveDeps.allVersions()) {
			modrinthApi.downloadPrimaryFile(modsDir, ver);
		}
		
		versionCache.save();
	}
	
	//Tries to parse the garbage that people write in the version number field into something comparable
	public static long goodSemVerParserYeah(String ver) {
		//Strip off everything after a + character, because that's defined to be the start of the comment section in semver or something like that
		if(ver.indexOf('+') != -1) ver = ver.substring(0, ver.indexOf('+'));
		
		//This pattern discards some trash then picks up a number in capturing group 1 
		Pattern someGarbageThenANumber = Pattern.compile("[^0-9]*([0-9]+)");
		Matcher matcher = someGarbageThenANumber.matcher(ver);
		long real = 0;
		while(matcher.find()) {
			real *= 1000;
			real += Integer.parseInt(matcher.group(1));
		}
		return real;
	}
}
