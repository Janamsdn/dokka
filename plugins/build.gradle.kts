subprojects {
    apply {
        plugin("maven-publish")
    }

    dependencies {
        compileOnly(project(":core"))
//        compileOnly(project(":coreDependencies", configuration = "shadow")) // uncomment if IntelliJ does not recognize pacakges from IntelliJ
        implementation(kotlin("stdlib-jdk8"))

        testImplementation(project(":testApi"))
        testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    }

    tasks.test {
        useJUnitPlatform()
        ignoreFailures = true
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}