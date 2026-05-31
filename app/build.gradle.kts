dependencies {
  // Firebase BoM & AI (Đã thay libs bằng version cứng)
  implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
  implementation("com.google.firebase:firebase-ai")
  
  // Google Sign-In & Auth SDK
  implementation("com.google.android.gms:play-services-auth:21.2.0")
  implementation("com.google.firebase:firebase-auth")

  // Core AndroidX & Compose
  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation(platform("androidx.compose:compose-bom:2024.10.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-core")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.navigation:navigation-compose:2.8.5")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

  // Room Database (Đã thay libs bằng version cứng)
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")

  // Networking & Image Loading (Đã thay libs bằng version cứng)
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("io.coil-kt:coil-compose:2.6.0")

  // Utilities
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  implementation("com.google.accompanist:accompanist-permissions:0.36.0")
  implementation("com.google.android.gms:play-services-location:21.3.0")
  implementation("androidx.datastore:datastore-preferences:1.1.1")

  // CameraX
  implementation("androidx.camera:camera-camera2:1.4.0")
  implementation("androidx.camera:camera-lifecycle:1.4.0")
  implementation("androidx.camera:camera-view:1.4.0")
  implementation("androidx.camera:camera-core:1.4.0")

  // Local Testing (Đã thay libs bằng version cứng)
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.robolectric:robolectric:4.13")
  testImplementation("io.github.takahirom.roborazzi:roborazzi:0.16.0")
  testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:0.16.0")
  testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:0.16.0")

  // Instrumented Testing (Đã thay libs bằng version cứng)
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
  androidTestImplementation("androidx.core:core-ktx:1.15.0")
  androidTestImplementation("androidx.test:runner:1.6.2")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
