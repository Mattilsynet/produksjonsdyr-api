plugins {
    id("org.springframework.boot") version "3.1.0"
    //id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    id ("org.openapi.generator") version "5.1.1"
    id ("java-library")
}

group = "no.mattilsynet"

repositories {
    mavenCentral()
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    outputDir.set("$buildDir/generated")
    ignoreFileOverride.set("$rootDir/.openapi-generator-ignore")
    inputSpec.set("$rootDir/spec.yaml")
    additionalProperties.set(mapOf("apiFirst" to "true"))
    apiPackage.set("no.mattilsynet.api")
    modelPackage.set("no.mattilsynet.model")
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "serializableModel" to "true",
        "useBeanValidation" to "true",
        "performBeanValuation" to "true",
        "enumPropertyNaming" to "UPPERCASE"
    ))
    sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).kotlin.srcDir("$buildDir/generated/src/main/kotlin")
}

tasks.register("openApiGenerate-task"){ dependsOn("openApiGenerate") }

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("pdyrapi-kotlin-spring")
    archiveClassifier.set("")
    isEnabled = true
}

tasks.bootJar {
    isEnabled = false
}

