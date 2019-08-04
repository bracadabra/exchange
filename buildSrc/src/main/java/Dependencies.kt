object Versions {
    const val kotlin = "1.3.41"
    const val androidx = "1.0.2"
    const val core = "1.0.2"
    const val arch = "1.1.1"
    const val okhttp = "3.12.0"
    const val retrofit = "2.6.0"
    const val rxJava = "2.2.10"
    const val rxAndroid = "2.1.1"
    const val rxBinding = "2.2.0"
    const val rxKotlin = "2.4.0"
    const val moshi = "1.8.0"
    const val timber = "4.7.1"
    const val dagger = "2.23.2"
    const val recyclerView = "1.0.0"
    const val adapterDelegates = "4.1.1"
}

@Suppress("unused")
object Dependencies {
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    const val appcompat = "androidx.appcompat:appcompat:${Versions.androidx}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.core}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"

    const val archViewModel = "android.arch.lifecycle:viewmodel:${Versions.arch}"
    const val archExtensions = "android.arch.lifecycle:extensions:${Versions.arch}"

    const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val rxBinding = "com.jakewharton.rxbinding2:rxbinding-kotlin:${Versions.rxBinding}"
    const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlin}"

    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiKotlinCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"

    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLogging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"

    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val rxJavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val daggerAndroid = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    const val daggerProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    const val adapterDelegates = "com.hannesdorfmann:adapterdelegates4:${Versions.adapterDelegates}"
}