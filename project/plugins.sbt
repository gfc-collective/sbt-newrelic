libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")
