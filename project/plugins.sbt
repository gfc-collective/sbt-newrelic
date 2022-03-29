libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.6.1")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.0")

addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.1")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.1")
