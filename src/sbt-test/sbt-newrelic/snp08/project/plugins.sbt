addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0-M2")

{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("com.gilt.sbt" % "sbt-newrelic" % pluginVersion)
}
