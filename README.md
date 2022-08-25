# quick-and-dirty-modfest-consumer

Consumes `https://platform.modfest.net/submissions/` and tries to download the modpack, along with any transitive dependencies marked on Modrinth. Also just kind of an exploration into Consuming APIs™ with java's new httpclient, which isn't bad.

Very rudimentary. Currently:

* Needs magical hardcoding to remove fabric-api/flk deps and add more deps. Not pluggable with a config file, and it doesn't look up the versions by slug.
* The transitive dependency version resolution algorithm seems to work okay for the modpack, but it is unsound.
* I haven't worked on packaging it into a standalone application yet either. `./gradlew run` to run it for now, the work dir is hardcoded to `./build/run`.

File versions on Modrinth are cached indefinitely (in `./modrinth_version_cache.json`), because it's more common for modders to upload a new version than it is to mutate an old one. Tries to respect Modrinth API ratelimiting too.

# License

CC0