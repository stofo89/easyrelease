package sk.stofo89.easyrelease.plugin

import java.text.DateFormat
import java.text.SimpleDateFormat

class Util {

    static final String TAG = '[easyrelease]'

    /**
     * Modifies output apk file name for all application variants
     */
    static def setApkName(project) {
        project.android.applicationVariants.all { variant ->

            // first check version code and name from Gradle build script, then from AndroidManifest.xml
            def versionCode = variant.versionCode ? variant.versionCode : getVersionCode(project)
            def versionName = variant.versionName ? variant.versionName : getVersionName(project)
            def timestamp = getDate();
            def fileName;
            def variantName = variant.name;
            if (variant.name.equalsIgnoreCase("developRelease")) {
                variantName = "test";
            } else if (variant.name.equalsIgnoreCase("productionRelease")) {
                variantName = "production";
            } else if (variant.name.equalsIgnoreCase("developDebug")) {
                variantName = "testDebug";
            } else if (variant.name.equalsIgnoreCase("productionDebug")) {
                variantName = "productionDebug";
            }

            if (variantName.equalsIgnoreCase("test") || variantName.equalsIgnoreCase("production")
                    || variantName.equalsIgnoreCase("release")) {
                fileName = "$project.name-$variantName-$versionName-${versionCode}-${timestamp}.apk"
            } else {
                println "$TAG DEBUG - only version"
                fileName = "$project.name-$variantName-$versionName-${versionCode}.apk"
            }

            variant.outputs.each { output ->
                output.outputFile = new File(output.outputFile.parent, fileName)
                println "$TAG Setting $output.name variant output name to $fileName"
            }
        }
    }

    /**
     * Parsing AndroidManifest.xml to return versionName
     */
    static def getVersionName(project) {
        def androidManifestPath = project.android.sourceSets.main.manifest.srcFile
        def manifestText = project.file(androidManifestPath).getText()
        def patternVersionNumber = java.util.regex.Pattern.compile("versionName=\"(\\d+)\\.(\\d+)\\.(\\d+)\"")
        def matcherVersionNumber = patternVersionNumber.matcher(manifestText)
        matcherVersionNumber.find()
        def majorVersion = Integer.parseInt(matcherVersionNumber.group(1))
        def minorVersion = Integer.parseInt(matcherVersionNumber.group(2))
        def pointVersion = Integer.parseInt(matcherVersionNumber.group(3))
        def versionName = majorVersion + "." + minorVersion + "." + pointVersion
        return versionName
    }
 
    /**
     * Parsing AndroidManifest.xml to return versionCode
     */
    static def getVersionCode(project) {
        def androidManifestPath = project.android.sourceSets.main.manifest.srcFile
        def manifestText = project.file(androidManifestPath).getText()
        def patternVersionNumber = java.util.regex.Pattern.compile("versionCode=\"(\\d+)\"")
        def matcherVersionNumber = patternVersionNumber.matcher(manifestText)
        matcherVersionNumber.find()
        def version = Integer.parseInt(matcherVersionNumber.group(1))
        return version
    }
 
    /**
     * Loads signing properties from file and sets them in release signingConfig
     */
    static def loadProperties(project) {
        def propFileName
        def propFile
        println "$TAG Reading SIGN LOCATION " + System.getenv("SIGN_LOCATION")
        if (System.getenv("SIGN_LOCATION") && new File(System.getenv("SIGN_LOCATION")).exists()) {
            propFileName = System.getenv("SIGN_LOCATION")
            propFile = new File("$propFileName")
        } else {
            propFileName = 'easyrelease.properties'
            def projectDir = project.projectDir
            propFile = new File("$projectDir/$propFileName")
        }

        println "$TAG Reading $propFileName"
        if (propFile.canRead()) {
            def props = new Properties()
            props.load(new FileInputStream(propFile))
                
            def buildType = project.android.signingConfigs.release
            buildType.storeFile = project.file(props["KEYSTORE_FILE"])
            buildType.storePassword = props["KEYSTORE_PASSWORD"]
            buildType.keyAlias = props["KEY_ALIAS"]
            buildType.keyPassword = props["KEY_PASSWORD"]

        } else {
            System.err.println "$TAG ERROR: Missing $propFileName"
        }
    }

    static def getDate() {
        DateFormat df = new SimpleDateFormat("YYYYMMddHHmm");
        return df.format(new Date());
    }
}