# quick-and-dirty-modfest-consumer

Consumes `https://platform.modfest.net/submissions/` and tries to download the modpack, along with any transitive dependencies marked on Modrinth. Also just kind of an exploration into Consuming APIs™ with java httpclient. It's not bad. 

Very rudimentary. Currently it needs magical hardcoding to remove fabric-api/flk deps. I haven't worked on packaging it into a .jar yet either, `./gradlew run` to run it for now.

# License

CC0