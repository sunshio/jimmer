plugins {
    `java-library`
    kotlin("jvm") version "1.6.10"
    antlr
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

dependencies {

    api(project(":jimmer-core"))
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    compileOnly("org.postgresql:postgresql:42.3.6")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    antlr("org.antlr:antlr4:4.9.3")

    testAnnotationProcessor(project(":jimmer-apt"))
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.springframework:spring-jdbc:5.3.20")
    testImplementation("com.fasterxml.uuid:java-uuid-generator:4.0.1")

    testImplementation("com.h2database:h2:2.1.212")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation("org.postgresql:postgresql:42.3.6")
    testImplementation(files("/Users/chentao/Downloads/ojdbc8-21.9.0.0.jar"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<Javadoc>{
    options.encoding = "UTF-8"
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
