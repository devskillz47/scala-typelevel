scalaVersion := "3.9.0"

lazy val catsEffectVersion    = "3.7.1"
lazy val doobieVersion        = "1.0.0-RC12"
lazy val http4sVersion        = "0.23.38"
lazy val circeGenericVersion  = "0.14.17"
lazy val circeFs2Version      = "0.14.1"
lazy val pureConfigVersion    = "0.17.11"
lazy val log4catsVersion      = "2.8.0"
lazy val slf4jVersion         = "2.0.20"
lazy val scalaTestVersion     = "3.2.20"
lazy val scalaTestCatsEffectVersion = "1.8.0"
lazy val testContainerVersion       = "1.21.4"


lazy val root = rootProject
  .settings(
    name := "scala-typelevel",
    libraryDependencies ++= Seq(
      //You can add library dependencies here, for example,
      //"org.scalatest" %% "scalatest" % "3.2.19" % Test,
      //"org.scalameta" %% "munit" % "1.2.3" % Test
      "org.typelevel"         %%  "cats-effect"           % catsEffectVersion,
      "org.tpolecat"          %%  "doobie-core"           % doobieVersion,
      "org.tpolecat"          %%  "doobie-hikari"         % doobieVersion,
      "org.tpolecat"          %%  "doobie-postgres"       % doobieVersion,
      "org.tpolecat"          %%  "doobie-scalatest"       % doobieVersion    % Test,
      "org.http4s"            %%  "http4s-dsl"            % http4sVersion,
      "org.http4s"            %%  "http4s-ember-server"   % http4sVersion,
      "org.http4s"            %%  "http4s-circe"          % http4sVersion,
      "io.circe"              %% "circe-generic"          % circeGenericVersion,
      "io.circe"              %% "circe-fs2"              % circeFs2Version,
      "com.github.pureconfig" %% "pureconfig-core"        % pureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"         % log4catsVersion,
      "org.slf4j"             % "slf4j-simple"            % slf4jVersion,
      "org.scalatest"         %% "scalatest"              % scalaTestVersion % Test,
      "org.typelevel"         %% "log4cats-noop"          % log4catsVersion  % Test,
      "org.scalatest"         %% "scalatest"              % scalaTestVersion % Test,
      "org.typelevel"         %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
      "org.testcontainers"    % "testcontainers"                % testContainerVersion        % Test,
      "org.testcontainers"    % "postgresql"                    % testContainerVersion        % Test,

  )
)
