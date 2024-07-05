# produksjonsdyr-api

Dette er produksjonsdyrdomenets openapi-spesifikasjon og skal brukes av interne løsninger som i første omgang dreier seg om register over anlegg og ansvar og på sikt av eksterne aktører som trenger tilgang på registerdata om anlegg og ansvar for dyr. 


[Visuell oversikt over domenemodellen](https://github.com/Mattilsynet/pdyr-arkitektur/blob/master/diagrammer/domene-modell-flere-bounded-contexts.drawio.png)


### Lokalt testbygg
Dra ned repo med git clone og bytt til ønsket branch.
```
./gradlew openApiGenerate-task                   
./gradlew build
./gradlew generateMetadataFileForMavenPublication
./gradlew generatePomFileForMavenPublication     
./gradlew publishToMavenLocal                    
```
Det bygges da en lokal utgave med SNAPSHOT-versjon f.eks "0.0.5-SNAPSHOT". 
Kommenter inn riktig verdi for variabelen pdyrapiVersion i build.gradle.kts


