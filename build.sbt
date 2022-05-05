import Dependencies._

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.13.6",
    organization := "fi.spectrumlabs",
    version := "0.1.0",
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case "logback.xml"                                             => MergeStrategy.first
      case "module-info.class"                                       => MergeStrategy.discard
      case other if other.contains("scala/annotation/nowarn.class")  => MergeStrategy.first
      case other if other.contains("scala/annotation/nowarn$.class") => MergeStrategy.first
      case other if other.contains("io.netty.versions")              => MergeStrategy.first
      case other                                                     => (assemblyMergeStrategy in assembly).value(other)
    },
    assembly / assemblyJarName := "mock-meta.jar",
    libraryDependencies ++= List(CompilerPlugins.betterMonadicFor, CompilerPlugins.kindProjector) ++ List(
        Libraries.tofuCore,
        Libraries.tofuDerivation,
        Libraries.tofuDoobie,
        Libraries.tofuLogging,
        Libraries.tofuStreams,
        Libraries.tofuZio,
        Libraries.tofuFs2,
        Libraries.doobiePg,
        Libraries.newtype,
        Libraries.mouse,
        Libraries.tapirCore,
        Libraries.tapirCirce,
        Libraries.derevoCirce,
        Libraries.enumeratum,
        Libraries.enumeratumCirce,
        Libraries.tofuConcurrent,
        Libraries.tofuOptics,
        Libraries.doobieHikari,
        Libraries.http4sServer,
        Libraries.tapirCirce,
        Libraries.tapirHttp4s,
        Libraries.tapirRedoc,
        Libraries.tapirDocs,
        Libraries.tapirOpenApi,
        Libraries.derevoPureconfig,
        Libraries.pureconfig
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.patch),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
  )
  .enablePlugins(JavaAppPackaging, UniversalPlugin, DockerPlugin)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Ymacro-annotations"
)