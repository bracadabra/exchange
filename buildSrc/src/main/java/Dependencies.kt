object Versions {
    const val kotlin = "1.3.31"
    const val androidx = "1.0.2"
    const val okhttp = "3.12.0"
    const val retrofit = "2.6.0"
    const val rxjava2 = "2.2.10"
    const val moshi = "1.8.0"
    const val timber = "4.7.1"
}

@Suppress("unused")
object Dependencies {
    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    val appcompat = "androidx.appcompat:appcompat:${Versions.androidx}"
    val coreKtx = "androidx.core:core-ktx:${Versions.androidx}"

    val rxjava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxjava2}"

    val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    val moshiKotlinCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"

    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"

    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val rxJavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    val timber = "com.jakewharton.timber:timber:${Versions.timber}"
}