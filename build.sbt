organization := "com.gilt.sbt"

name := "sbt-newrelic"

sbtPlugin := true

crossSbtVersions := List("0.13.16", "1.0.0")

enablePlugins(GitVersioning, GitBranchPrompt)

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false

git.useGitDescribe := true

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-encoding", "UTF-8"
)

libraryDependencies += {
  val currentSbtVersion = (sbtBinaryVersion in pluginCrossBuild).value
  Defaults.sbtPluginExtra("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2-RC2" % "provided", currentSbtVersion, scalaBinaryVersion.value)
}
//addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2-RC2" % "provided")

publishMavenStyle := false

bintrayOrganization := Some("giltgroupe")

bintrayPackageLabels := Seq("sbt", "newrelic", "sbt-native-packager")

bintrayRepository := "sbt-plugin-releases"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
