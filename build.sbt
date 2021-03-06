val zioVersion     = "1.0.7"
val zioJsonVersion = "0.1.4"
val zioNioVersion  = "1.0.0-RC10"

scalaVersion := "2.13.5"

enablePlugins(JavaAppPackaging)

addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("prepare", "fix; fmt")

lazy val root =
  project
    .in(file("."))
    .settings(Compile / mainClass := Some("io.timmers.pws.Application"))
    .aggregate(pwsApi)
    .dependsOn(pwsApi)

lazy val pwsApi =
  module("pws-api", "pws")
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio-json" % zioJsonVersion,
        "dev.zio" %% "zio-nio"  % zioNioVersion
      )
    )

def module(moduleName: String, fileName: String): Project =
  Project(moduleName, file(fileName))
    .settings(
      scalaVersion := "2.13.5",
      organization := "io.timmers",
      name := moduleName,
      version := "0.1.0",
      scalacOptions := Seq(
        "-Ywarn-unused:_",
        "-Xfatal-warnings"
      ),
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"         % zioVersion,
        "dev.zio" %% "zio-streams" % zioVersion
      ),
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio-test"     % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test
      ),
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      semanticdbEnabled := true,
      semanticdbVersion := scalafixSemanticdb.revision,
      ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(
        scalaVersion.value
      ),
      ThisBuild / scalafixDependencies ++= List(
        "com.github.liancheng" %% "organize-imports" % "0.4.0",
        "com.github.vovapolu"  %% "scaluzzi"         % "0.1.12"
      )
    )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}
