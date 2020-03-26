rootProject.name = "manifest-plugin"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

plugins {
    id("com.gradle.enterprise").version("3.0")
}
