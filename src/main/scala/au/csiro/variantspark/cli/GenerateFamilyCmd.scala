package au.csiro.variantspark.cli

import java.io.File

import scala.Range

import org.apache.commons.lang3.builder.ToStringBuilder
import au.csiro.pbdava.ssparkle.common.utils.Logging
import org.kohsuke.args4j.Option

import au.csiro.pbdava.ssparkle.common.arg4j.AppRunner
import au.csiro.pbdava.ssparkle.common.arg4j.TestArgs
import au.csiro.sparkle.common.args4j.ArgsApp
import au.csiro.pbdava.ssparkle.spark.SparkApp
import au.csiro.variantspark.cmd.EchoUtils._
import au.csiro.variantspark.cmd.Echoable

import au.csiro.variantspark.pedigree.ReferenceContigSet
import is.hail.HailContext
import au.csiro.variantspark.hail.family.GenerateFamily
import au.csiro.variantspark.pedigree.FamilySpec
import au.csiro.variantspark.pedigree.SimpleHomozigotSpecFactory
import au.csiro.variantspark.pedigree.PedigreeTree
import au.csiro.variantspark.hail._

class GenerateFamilyCmd extends ArgsApp with SparkApp with Logging with TestArgs with Echoable {

  override def createConf = super.createConf
      .set("spark.sql.files.openCostInBytes", "53687091200") // 50GB : min for hail 
      .set("spark.sql.files.maxPartitionBytes", "53687091200") // 50GB : min for hail 
  
  @Option(name="-if", required=true, usage="Path to input vcf file", aliases=Array("--input-file"))
  val inputFile:String = null

  @Option(name="-of", required=true, usage="Path to output vcf file", aliases=Array("--output-file"))
  val outputFile:String = null

  @Option(name="-pf", required=true, usage="Path to pedigree file", aliases=Array("--ped-file"))
  val pedFile:String = null
  
  @Override
  def testArgs = Array("-if", "data/hipsterIndex/hipster.vcf.bgz", 
      "-of", "target/g1k_ceu_family_15_2.vcf",
      "-pf", "data/relatedness/g1k_ceu_family_15_2.ped"
      )      
  
  @Override
  def run():Unit = {
    logInfo("Running with params: " + ToStringBuilder.reflectionToString(this))
    val hc = HailContext(sc)
    echo(s"Loadig vcf from ${inputFile}")
    val gds = hc.importVCFGenericEx(inputFile)
    echo(s"Loading pedigree from: ${pedFile}") 
    
    val tree = PedigreeTree.loadPed(pedFile)
    val gameteFactory  = new SimpleHomozigotSpecFactory(ReferenceContigSet.b37)
    val familySpec = FamilySpec.apply(tree, gameteFactory)
    val familyGds = GenerateFamily(familySpec)(gds)
    echo(s"Saving family vcf to: ${outputFile}")
    familyGds.exportVCFEx(outputFile)
  }
}

object GenerateFamilyCmd  {
  def main(args:Array[String]) {
    AppRunner.mains[GenerateFamilyCmd](args)
  }
}