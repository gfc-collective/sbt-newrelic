package com.gilt.sbt.newrelic

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport._
import com.typesafe.sbt.packager.archetypes.TemplateWriter

object NewRelic extends AutoPlugin {

  object autoImport {
    val newrelicVersion = settingKey[String]("New Relic version")
    val newrelicAgent = taskKey[File]("New Relic agent jar location")
    val newrelicAppName = settingKey[String]("App Name reported to New Relic monitoring")
    val newrelicAttributesEnabled = settingKey[Boolean]("Enable sending of attributes to New Relic")
    val newrelicBrowserInstrumentation = settingKey[Boolean]("Enable automatic Real User Monitoring")
    val newrelicConfig = taskKey[File]("Generates a New Relic configuration file")
    val newrelicConfigTemplate = settingKey[java.net.URL]("Location of New Relic configuration template")
    val newrelicLicenseKey = settingKey[Option[String]]("License Key for New Relic account")
    val newrelicAkkaInstrumentation = settingKey[Boolean]("Specifies whether Akka instrumentation is enabled")
    val newrelicCustomTracing = settingKey[Boolean]("Option to scan and instrument @Trace annotations")
    val newrelicIgnoreErrors = settingKey[Seq[String]]("List of exceptions that New Relic should not report as errors")
    val newrelicTemplateReplacements = settingKey[Seq[(String, String)]]("Replacements for New Relic configuration template")
    val newrelicIncludeApi = settingKey[Boolean]("Add New Relic API artifacts to library dependencies")
  }

  import autoImport._

  override def requires = JavaAppPackaging

  val nrConfig = config("newrelic-agent").hide

  override lazy val projectSettings = Seq(
    ivyConfigurations += nrConfig,
    newrelicVersion := "3.32.0",
    newrelicAgent := findNewrelicAgent(update.value),
    newrelicAppName := name.value,
    newrelicAttributesEnabled := true,
    newrelicBrowserInstrumentation := true,
    newrelicConfig := makeNewRelicConfig((target in Universal).value, newrelicConfigTemplate.value, newrelicTemplateReplacements.value),
    newrelicConfigTemplate := getNewrelicConfigTemplate,
    newrelicLicenseKey := None,
    newrelicAkkaInstrumentation := true,
    newrelicCustomTracing := false,
    newrelicIgnoreErrors := Seq("akka.actor.ActorKilledException"),
    newrelicTemplateReplacements := Seq(
      "app_name" -> newrelicAppName.value,
      "license_key" -> newrelicLicenseKey.value.getOrElse(""),
      "akka_instrumentation_enabled" -> newrelicAkkaInstrumentation.value.toString,
      "custom_tracing" -> newrelicCustomTracing.value.toString,
      "attributes_enabled" -> newrelicAttributesEnabled.value.toString,
      "browser_monitoring" -> newrelicBrowserInstrumentation.value.toString,
      "ignore_errors" -> newrelicIgnoreErrors.value.mkString(",")
    ),
    newrelicIncludeApi := false,
    libraryDependencies += "com.newrelic.agent.java" % "newrelic-agent" % newrelicVersion.value % nrConfig,
    libraryDependencies ++= {
      if (newrelicIncludeApi.value)
        Seq("com.newrelic.agent.java" % "newrelic-api" % newrelicVersion.value)
      else
        Seq.empty
    },
    mappings in Universal ++= Seq(
      newrelicAgent.value -> "newrelic/newrelic.jar",
      newrelicConfig.value -> "newrelic/newrelic.yml"
    ),
    bashScriptExtraDefines += """addJava "-javaagent:${app_home}/../newrelic/newrelic.jar"""",
    batScriptExtraDefines += """set "_JAVA_OPTS=%_JAVA_OPTS% -javaagent:%@@APP_ENV_NAME@@_HOME%\\newrelic\\newrelic.jar""""
  )

  private[newrelic] def makeNewRelicConfig(tmpDir: File, source: java.net.URL, replacements: Seq[(String, String)]): File = {
    val fileContents = TemplateWriter.generateScript(source, replacements)
    val nrFile = tmpDir / "tmp" / "newrelic.yml"
    IO.write(nrFile, fileContents)
    nrFile
  }

  protected def getNewrelicConfigTemplate: java.net.URL = getClass.getResource("newrelic.yml.template")

  private[this] val newRelicFilter: DependencyFilter =
    configurationFilter("newrelic-agent") && artifactFilter(`type` = "jar")

  def findNewrelicAgent(report: UpdateReport) = report.matching(newRelicFilter).head
}
