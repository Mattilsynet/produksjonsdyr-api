import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
    id("org.openapi.generator") version "7.21.0"
    id("java-library")
    id("maven-publish")
}

group = "no.mattilsynet.produksjonsdyr"
version = "0.0.64-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

val openApiWebMvcApiVersion = "3.0.2"

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:$openApiWebMvcApiVersion")
    implementation("org.springframework:spring-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("jakarta.validation:jakarta.validation-api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val openapiSpecs =
    mapOf(
        "berikelseapi" to "$rootDir/reference/BerikelseAPI.yaml",
        "frontendsupportapi" to "$rootDir/reference/FrontendSupportAPI.yaml",
        "brukerinfoapi" to "$rootDir/reference/Brukerinfo.yaml",
        "ansvarapi" to "$rootDir/reference/Ansvar.yaml",
        "anleggapi" to "$rootDir/reference/Anlegg.yaml",
        "oppsummering" to "$rootDir/reference/Oppsummering.yaml",
        "anleggrapportering" to "$rootDir/reference/AnleggRapportering.yaml",
        "vedlikehold" to "$rootDir/reference/Vedlikehold.yaml",
        "internAnleggOppdatering" to "$rootDir/reference/InternAnleggOppdatering.yaml",
    )

openapiSpecs.forEach {
    tasks.register("openApiGenerate-${it.key}", GenerateTask::class) {
        group = "openapi"
        description = "Generates OpenAPI spec for ${it.key}"
        ignoreFileOverride.set("$rootDir/.openapi-generator-ignore")
        generatorName.set("kotlin-spring")
        additionalProperties.set(
            mapOf(
                "serializationLibrary" to "jackson",
                "apiFirst" to "true",
                "removeEnumValuePrefix" to "false",
            ),
        )
        inputSpec.set(it.value)
        outputDir.set("$buildDir/generated")
        apiPackage.set("no.mattilsynet.api")
        modelPackage.set("no.mattilsynet.model")
        modelNameSuffix.set("Dto")
        configOptions.set(
            mapOf(
                "useSpringBoot3" to "true",
                "library" to "spring-boot",
                "serializationLibrary" to "jackson",
                "interfaceOnly" to "true",
                "serializableModel" to "true",
                "useBeanValidation" to "true",
                "performBeanValidation" to "true",
                "enumPropertyNaming" to "UPPERCASE",
                "jakarta" to "true",
            ),
        )
        sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).kotlin.srcDir("$buildDir/generated/src/main/kotlin")
    }
}

tasks.register("openApiGenerate-task") {
    group = "openapi"
    description = "Generates OpenAPI spec for all tasks"
    dependsOn(openapiSpecs.keys.map { "openApiGenerate-$it" })
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate-task")
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
