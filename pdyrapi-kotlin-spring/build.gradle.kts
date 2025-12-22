import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.openapi.generator") version "7.17.0"
    id("maven-publish")
    id("org.sonarqube") version "latest.release"
    `java-library`
}

group = "no.mattilsynet.produksjonsdyr"
version = "0.0.64-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-web:7.0.2")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
    implementation("io.swagger.core.v3:swagger-models:2.2.41")
    // implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")

    // implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    // implementation("javax.servlet:servlet-api:4.0.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.14.0")
    //    exclude(group = "org.slf4j", module = "slf4j-simple")
    //    exclude(group = "ch.qos.logback", module = "logback-classic")
    // }
    // implementation("javax.servlet:servlet-api:2.5")
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
        "internAnleggOppdatering" to "reference/InternAnleggOppdatering.yaml",
    )

openapiSpecs.forEach {
    tasks.register<GenerateTask>("openApiGenerate-${it.key}") {
        group = "openapi"
        ignoreFileOverride.set("$rootDir/.openapi-generator-ignore")
        generatorName.set("kotlin-spring")
        additionalProperties.set(
            mapOf(
                "apiFirst" to "true",
                "removeEnumValuePrefix" to "false",
                "useJakartaEe" to "true",
            ),
        )
        inputSpec.set("$rootDir/${it.value}")
        outputDir.set(
            layout.buildDirectory
                .dir("generated")
                .get()
                .asFile.absolutePath,
        )
        apiPackage.set("no.mattilsynet.api")
        modelPackage.set("no.mattilsynet.model")
        modelNameSuffix.set("Dto")
        configOptions.set(
            mapOf(
                "interfaceOnly" to "true",
                "serializableModel" to "true",
                "useBeanValidation" to "true",
                "performBeanValuation" to "true",
                "enumPropertyNaming" to "UPPERCASE",
            ),
        )
        sourceSets
            .getByName(
                SourceSet.MAIN_SOURCE_SET_NAME,
            ).kotlin
            .srcDir(
                layout.buildDirectory
                    .dir("generated/src/main/kotlin")
                    .get()
                    .asFile.absolutePath,
            )
    }
}
tasks.register("openApiGenerate-task") { dependsOn(openapiSpecs.keys.map { "openApiGenerate-$it" }) }

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
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
