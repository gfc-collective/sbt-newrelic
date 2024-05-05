organization := "com.gilt.sbt"

name := "sbt-newrelic"

scalaVersion := "2.12.15"

crossScalaVersions := Seq(scalaVersion.value, "2.13.14")

crossSbtVersions := List("0.13.18", "1.3.6")

enablePlugins(SbtPlugin, GitVersioning, GitBranchPrompt)

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

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9" % "provided")

publishMavenStyle := false

bintrayOrganization := Some("giltgroupe")

bintrayPackageLabels := Seq("sbt", "newrelic", "sbt-native-packager")

bintrayRepository := "sbt-plugin-releases"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
