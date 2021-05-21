libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")

addSbtPlugin("io.crashbox" % "sbt-gpg" % "0.2.1")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.2")
