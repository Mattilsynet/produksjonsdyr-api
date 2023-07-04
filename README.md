



# produksjonsdyr-api

### Lokalt testbygg:
Dra ned repo med git clone og bytt til ønsket branch.
```
./gradlew openApiGenerate-task                   
./gradlew build
./gradlew generateMetadataFileForMavenPublication
./gradlew generatePomFileForMavenPublication     
./gradlew publishToMavenLocal                    
```
Det bygges da en lokal utgave med versjon "0.0.5-SNAPSHOT"
Kommenter inn riktig verdi for variabelen pdyrapiVersion i build.gradle.kts


