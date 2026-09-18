import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

@CacheableTask
abstract class GenerateSongDatabase @Inject constructor(
    private val execOperations: ExecOperations,
) : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceXml: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schemaSql: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val generatorScript: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val output = outputDirectory.file("songs.db").get().asFile
        output.parentFile.mkdirs()
        execOperations.exec {
            commandLine(
                "python3",
                generatorScript.get().asFile.absolutePath,
                "generate",
                "--source", sourceXml.get().asFile.absolutePath,
                "--schema", schemaSql.get().asFile.absolutePath,
                "--output", output.absolutePath,
                "--expected-count", "414",
            )
        }.assertNormalExitValue()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencyLocking {
    lockAllConfigurations()
}

val catalogSource = layout.projectDirectory.file("src/main/assets/songs.xml")
val catalogSchema = rootProject.layout.projectDirectory.file("tools/catalog/schema.sql")
val catalogGenerator = rootProject.layout.projectDirectory.file("tools/catalog/generate_song_database.py")
val catalogBaseline = layout.projectDirectory.file("src/test/resources/catalog-baseline.xml")
val generatedCatalogAssets = layout.buildDirectory.dir("generated/songCatalog/assets")
val generatedCatalogDatabase = generatedCatalogAssets.map { it.file("songs.db") }

val generateSongDatabase = tasks.register<GenerateSongDatabase>("generateSongDatabase") {
    group = "build"
    description = "Generates the packaged SQLite song catalog from canonical XML."
    sourceXml.set(catalogSource)
    schemaSql.set(catalogSchema)
    generatorScript.set(catalogGenerator)
    outputDirectory.set(generatedCatalogAssets)
}

val testSongCatalogGenerator = tasks.register<Exec>("testSongCatalogGenerator") {
    group = "verification"
    description = "Runs the Python catalog generator tests."
    workingDir(rootProject.projectDir)
    commandLine("python3", "-m", "unittest", "discover", "-s", "tools/catalog/tests", "-p", "test_*.py")
    inputs.dir(rootProject.layout.projectDirectory.dir("tools/catalog/tests"))
    inputs.files(catalogSource, catalogSchema, catalogGenerator, catalogBaseline)
}

val verifySongCatalog = tasks.register<Exec>("verifySongCatalog") {
    group = "verification"
    description = "Verifies generated SQLite content against XML and the historical baseline."
    dependsOn(generateSongDatabase, testSongCatalogGenerator)
    workingDir(rootProject.projectDir)
    commandLine(
        "python3",
        catalogGenerator.asFile.absolutePath,
        "verify",
        "--source", catalogSource.asFile.absolutePath,
        "--schema", catalogSchema.asFile.absolutePath,
        "--database", generatedCatalogDatabase.get().asFile.absolutePath,
        "--baseline", catalogBaseline.asFile.absolutePath,
        "--expected-count", "414",
    )
    inputs.files(catalogSource, catalogSchema, catalogGenerator, catalogBaseline)
    inputs.file(generatedCatalogDatabase)
}

android {
    namespace = "com.goodtrendltd.HolySongs"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.goodtrendltd.HolySongs"
        minSdk = 23
        targetSdk = 37
        versionCode = 8
        versionName = "2.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.all {
            it.systemProperty("repositoryRoot", rootProject.projectDir.absolutePath)
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            generateSongDatabase,
            GenerateSongDatabase::outputDirectory,
        )
    }
}

tasks.named("check").configure {
    dependsOn(verifySongCatalog)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation("com.belerweb:pinyin4j:2.5.0")

    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.09.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")

    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
