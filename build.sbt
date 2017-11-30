name := "date-folders"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies += "commons-io" % "commons-io" % "2.4"
libraryDependencies += "joda-time" % "joda-time" % "2.7"

scalafmtOnCompile in ThisBuild := true
