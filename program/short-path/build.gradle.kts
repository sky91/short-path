plugins {
    application
}

repositories {
    maven("https://maven.aliyun.com/repository/public/")
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.alibaba:fastjson:1.2.55")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.springframework.boot:spring-boot-starter-web:2.1.3.RELEASE")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "x.flyspace.shortpath.WebMain"
}