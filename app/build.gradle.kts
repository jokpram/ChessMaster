plugins {
    id("java")
    id("application")
}

group = "com.chessmaster"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.chessmaster.ChessApplication")
}

dependencies {
    // Google Guava for utility functions
    implementation("com.google.guava:guava:32.1.3-jre")
}

// Create distribution with all dependencies
tasks.register<Jar>("fatJar") {
    archiveBaseName.set("ChessMaster")
    archiveVersion.set("${project.version}")
    archiveClassifier.set("all")
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes["Main-Class"] = "com.chessmaster.ChessApplication"
        attributes["Implementation-Title"] = "ChessMaster Pro"
        attributes["Implementation-Version"] = "${project.version}"
    }
    
    from(sourceSets.main.get().output)
    
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

// Create distribution zip with start scripts
tasks.named<CreateStartScripts>("startScripts") {
    applicationName = "ChessMaster"
}

tasks.named<Zip>("distZip") {
    archiveBaseName.set("ChessMaster")
}

tasks.named<Tar>("distTar") {
    archiveBaseName.set("ChessMaster")
}

// Create Windows app image with jpackage
tasks.register<Exec>("createAppImage") {
    dependsOn("installDist")
    
    val installDir = layout.buildDirectory.dir("install/app")
    val outputDir = layout.buildDirectory.dir("jpackage")
    val javaHome = System.getProperty("java.home")
    
    doFirst {
        outputDir.get().asFile.mkdirs()
    }
    
    commandLine(
        "$javaHome/bin/jpackage",
        "--type", "app-image",
        "--name", "ChessMaster",
        "--input", "${installDir.get().asFile}/lib",
        "--main-jar", "app-${project.version}.jar",
        "--main-class", "com.chessmaster.ChessApplication",
        "--dest", outputDir.get().asFile.absolutePath,
        "--app-version", "${project.version}",
        "--vendor", "ChessMaster Inc.",
        "--description", "Professional Chess Game with AI"
    )
}

// Create Windows EXE installer with jpackage
tasks.register<Exec>("createExeInstaller") {
    dependsOn("installDist")
    
    val installDir = layout.buildDirectory.dir("install/app")
    val outputDir = layout.buildDirectory.dir("installer")
    val javaHome = System.getProperty("java.home")
    
    doFirst {
        outputDir.get().asFile.mkdirs()
    }
    
    commandLine(
        "$javaHome/bin/jpackage",
        "--type", "exe",
        "--name", "ChessMaster",
        "--input", "${installDir.get().asFile}/lib",
        "--main-jar", "app-${project.version}.jar",
        "--main-class", "com.chessmaster.ChessApplication",
        "--dest", outputDir.get().asFile.absolutePath,
        "--app-version", "${project.version}",
        "--vendor", "ChessMaster Inc.",
        "--description", "Professional Chess Game with AI",
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut",
        "--win-shortcut-prompt"
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Create SetupChessMaster.exe and copy to dist folder
tasks.register<Copy>("createSetupExe") {
    dependsOn("createExeInstaller")
    
    from(layout.buildDirectory.dir("installer"))
    into(rootProject.layout.projectDirectory.dir("dist"))
    include("ChessMaster-*.exe")
    rename { "SetupChessMaster.exe" }
}
