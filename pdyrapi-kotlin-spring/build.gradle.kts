plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("org.openapi.generator") version "7.9.0"
    id("java-library")
    id("maven-publish")
    id("org.sonarqube") version "5.1.0.4882"
}

val openapiGeneratorVersion by extra("7.9.0")
val swaggerVersion by extra("2.2.25")
val jakartaValidationApiVersion by extra("3.1.0")
val jakartaServletApiVersion by extra("6.1.0")

group = "no.mattilsynet.produksjonsdyr"
version = "0.0.63-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.swagger.core.v3:swagger-annotations:$swaggerVersion")
    implementation("io.swagger.core.v3:swagger-models:$swaggerVersion")
    implementation("jakarta.servlet:jakarta.servlet-api:$jakartaServletApiVersion")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    implementation("org.openapitools:openapi-generator-gradle-plugin:$openapiGeneratorVersion") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val openapiSpecs =
    mapOf(
        "berikelseapi" to "reference/BerikelseAPI.yaml",
        "frontendsupportapi" to "reference/FrontendSupportAPI.yaml",
        "brukerinfoapi" to "reference/Brukerinfo.yaml",
        "ansvarapi" to "reference/Ansvar.yaml",
        "anleggapi" to "reference/Anlegg.yaml",
        "oppsummering" to "reference/Oppsummering.yaml",
        "anleggrapportering" to "reference/AnleggRapportering.yaml",
        "vedlikehold" to "reference/Vedlikehold.yaml",
    )

openapiSpecs.forEach {
    tasks.create("openApiGenerate-${it.key}", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
        group = "openapi"
        description = "Generates code from OpenAPI spec ${it.value}"
        ignoreFileOverride.set("$rootDir/.openapi-generator-ignore")
        generatorName.set("kotlin-spring")
        additionalProperties.set(
            mapOf(
                "apiFirst" to "true",
                "removeEnumValuePrefix" to "false",
            ),
        )
        inputSpec.set("$rootDir/${it.value}")
        outputDir.set("$projectDir/build/generated")
        apiPackage.set("no.mattilsynet.api")
        modelPackage.set("no.mattilsynet.model")
        modelNameSuffix.set("Dto")
        configOptions.set(
            mapOf(
                "useSpringBoot3" to "true",
                "interfaceOnly" to "true",
                "serializableModel" to "true",
                "useBeanValidation" to "true",
                "performBeanValuation" to "true",
                "enumPropertyNaming" to "UPPERCASE",
            ),
        )
        sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).kotlin.srcDir("$projectDir/build/generated/src/main/kotlin")
    }
}

tasks.register("openApiGenerate-task") {
    group = "openapi"
    description = "Generates code from all OpenAPI specs"
    dependsOn(openapiSpecs.keys.map { "openApiGenerate-$it" })
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    isEnabled = true
    archiveClassifier.set("") // fjerner plain fra navnet til jar-filen
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
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
