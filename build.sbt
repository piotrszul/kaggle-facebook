import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "kaggle-facebook"

version := "1.0"

scalaVersion := "2.10.2"

// copied from scalding's build.sbt

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.2.1" % "provided" withSources()

libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % "1.2.1"  % "provided" withSources()

libraryDependencies += "org.apache.spark" % "spark-hive_2.10" % "1.2.1"  % "provided" withSources()

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

// Invocation exception if we try to run the tests in parallel
parallelExecution in Test := false

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case s if s.endsWith(".class") => MergeStrategy.last
        case s if s.endsWith("project.clj") => MergeStrategy.concat
        case s if s.endsWith(".html") => MergeStrategy.last
        case s if s.endsWith(".dtd") => MergeStrategy.last
        case s if s.endsWith(".xsd") => MergeStrategy.last
        case x => old(x)
      }
}


