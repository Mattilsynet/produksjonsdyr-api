plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    id ("org.openapi.generator") version "6.4.0"
    id ("java-library")
    id("maven-publish")
}

group = "no.mattilsynet.produksjonsdyr"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-web:6.0.6")
    implementation("javax.servlet:servlet-api:2.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation ("org.openapitools:openapi-generator-gradle-plugin:6.2.1"){
        exclude(group = "org.slf4j", module = "slf4j-simple")
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    implementation("javax.servlet:servlet-api:2.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    isEnabled = true
}

tasks.bootJar {
    isEnabled = false
}

if (project.hasProperty("releaseVersion")) {
    version = project.properties["releaseVersion"]!!
}

publishing {
    repositories {
        maven {
            name = "MattilsynetGitHubPackages"
            url = uri("https://maven.pkg.github.com/Mattilsynet/produksjonsdyr-api")
            credentials {
                username = (project.findProperty("githubActor") ?: System.getenv("GITHUB_ACTOR")) as String?
                password = (project.findProperty("githubToken") ?: System.getenv("GITHUB_TOKEN")) as String?
            }
        }
    }
    publications {
        register("pdyr", MavenPublication::class) {
            from(components["java"])
        }
    }
}

