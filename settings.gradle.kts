plugins {
    id("com.gradle.develocity") version ("3.18.2")
}

rootProject.name = "manifest-plugin"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"

        publishing.onlyIf { false }
        if (!System.getenv("CI").isNullOrEmpty()) {
            publishing.onlyIf { true }
            tag("CI")
        }
    }
}
