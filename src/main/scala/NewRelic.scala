package com.gilt.sbt.newrelic

import sbt._
import sbt.Keys._

import com.gilt.sbt.newrelic.compat.DependencyFilter
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin.autoImport._
import com.typesafe.sbt.packager.archetypes.scripts.BatStartScriptPlugin
import com.typesafe.sbt.packager.archetypes.scripts.BatStartScriptPlugin.autoImport._
import com.typesafe.sbt.packager.archetypes.TemplateWriter

object NewRelic extends AutoPlugin {

  object autoImport {
    object NewRelicLogLevel {
      sealed trait LogLevel extends Product with Serializable
      case object OFF extends LogLevel
      case object SEVERE extends LogLevel
      case object WARNING extends LogLevel
      case object INFO extends LogLevel
      case object FINE extends LogLevel
      case object FINER extends LogLevel
      case object FINEST extends LogLevel
    }

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
    val newrelicLogDir = settingKey[String]("The directory for the newrelic agent log file. Default is the newrelic default (the logs directory under the newrelic.jar directory).")
    val newrelicLogLevel = settingKey[NewRelicLogLevel.LogLevel]("Specify the log level of the NewRelic agent. Default is `info`.")
    val newrelicAuditMode = settingKey[Boolean]("Log all data sent to and from New Relic in plain text. Default is `false`.")
    val newrelicIgnoreStatusCodes = settingKey[Seq[Int]]("List of HTTP status codes that New Relic should not report as errors. Default is `404`.")
    val newrelicFuturesAsSegments = settingKey[Boolean]("Report Scala Futures as transaction segments.")
    val newrelicLiteMode = settingKey[Boolean]("Use lite mode.")
  }

  import autoImport._

  override def requires = BashStartScriptPlugin && BatStartScriptPlugin

  val NrConfig = config("newrelic-agent").hide
  @deprecated("use NrConfig", "")
  val nrConfig = NrConfig

  override lazy val projectSettings = Seq(
    ivyConfigurations += NrConfig,
    newrelicVersion := "4.2.0",
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
    newrelicLogLevel := NewRelicLogLevel.INFO,
    newrelicAuditMode := false,
    newrelicIgnoreStatusCodes := Seq(404),
    newrelicFuturesAsSegments := false,
    newrelicLiteMode := false,
    newrelicTemplateReplacements := Seq(
      "app_name" -> newrelicAppName.value,
      "license_key" -> newrelicLicenseKey.value.getOrElse(""),
      "akka_instrumentation_enabled" -> newrelicAkkaInstrumentation.value.toString,
      "custom_tracing" -> newrelicCustomTracing.value.toString,
      "attributes_enabled" -> newrelicAttributesEnabled.value.toString,
      "browser_monitoring" -> newrelicBrowserInstrumentation.value.toString,
      "ignore_errors" -> newrelicIgnoreErrors.value.mkString(","),
      "log_file_path" -> resolveNewrelicLogDir(newrelicLogDir.?.value),
      "log_level" -> newrelicLogLevel.value.toString.toLowerCase,
      "audit_mode" -> newrelicAuditMode.value.toString,
      "ignore_status_codes" -> newrelicIgnoreStatusCodes.value.mkString(","),
      "scala_futures_as_segments" -> newrelicFuturesAsSegments.value.toString,
      "lite_mode" -> newrelicLiteMode.value.toString
    ),
    newrelicIncludeApi := false,
    libraryDependencies += "com.newrelic.agent.java" % "newrelic-agent" % newrelicVersion.value % NrConfig,
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

  private def resolveNewrelicLogDir(sbtSettingValueOpt: Option[String]): String = sbtSettingValueOpt.fold("#log_file_path:")("log_file_path: " + _)

}
