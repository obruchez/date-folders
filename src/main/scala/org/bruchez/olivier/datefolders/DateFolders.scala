package org.bruchez.olivier.datefolders

import java.io.File

import org.apache.commons.io.FilenameUtils
import org.joda.time.LocalDate

object DateFolders {
  def main(args: Array[String]): Unit = {
    if (args.length == 1) {
      moveFilesIn(new File(args(0)))
    } else {
      println("Missing argument: <directory to process>")
    }
  }

  def moveFilesIn(directory: File): Unit = {
    println(s"Processing ${directory.getCanonicalPath}...")

    val allFiles = filesInDirectory(directory, recursive = false, includeDirectories = false)
    val filesToProcess = allFiles.filterNot(file => isOsMetadataFile(file.getName))

    println(s"${filesToProcess.size} file(s) to process")

    for {
      file <- filesToProcess
      lastModificationDate = new LocalDate(file.lastModified())
      year = lastModificationDate.getYear
      month = lastModificationDate.getMonthOfYear
      day = lastModificationDate.getDayOfMonth
      subDirectory = f"$year%04d.$month%02d.$day%02d"
    } {
      val destinationDirectory = new File(file.getParentFile, subDirectory)
      val destinationFile = new File(destinationDirectory, file.getName)

      println(s"Moving ${file.getCanonicalPath} to ${destinationFile.getCanonicalPath}")
      destinationDirectory.mkdirs()
      file.renameTo(destinationFile)
    }
  }

  def baseNameAndExtension(filename: String): (String, Option[String]) =
    (
      FilenameUtils.getBaseName(filename),
      Some(FilenameUtils.getExtension(filename)).filterNot(_.isEmpty)
    )

  def baseNameAndExtension(file: File): (String, Option[String]) =
    baseNameAndExtension(file.getAbsolutePath)

  def filesInDirectory(
      directory: File,
      recursive: Boolean,
      includeDirectories: Boolean
  ): Seq[File] = {
    val (directories, files) =
      Option(directory.listFiles()).fold(Seq[File]())(_.toSeq).partition(_.isDirectory)
    val subDirectoriesAndFiles =
      if (recursive) {
        directories.flatMap(filesInDirectory(_, recursive = true, includeDirectories))
      } else {
        Seq()
      }
    (if (includeDirectories) directories else Seq()) ++ files ++ subDirectoriesAndFiles
  }

  def isMacOsMetadataFile(filename: String): Boolean =
    filename == MacOsDsStoreFilename || filename.startsWith(MacOsMetadataFilePrefix)

  private val MacOsDsStoreFilename = ".DS_Store"
  private val MacOsMetadataFilePrefix = "._"

  def isOsMetadataFile(filename: String): Boolean =
    isMacOsMetadataFile(filename) || isWindowsMetadataFile(filename)

  def isWindowsMetadataFile(filename: String): Boolean =
    filename == WindowsThumbsDbFilename

  private val WindowsThumbsDbFilename = "Thumbs.db"
}
