package au.csiro.variantspark.hail.family


import au.csiro.variantspark.test.SparkTest
import org.junit.Test
import org.junit.Assert._

import is.hail.HailContext
import is.hail.variant._
import is.hail.utils._
import is.hail.expr._
import au.csiro.variantspark.hail._
import is.hail.variant.Variant
import is.hail.variant.GenotypeBuilder
import is.hail.variant.GenotypeStreamBuilder
import is.hail.variant.Genotype
import is.hail.annotations.Annotation
import is.hail.variant.GTPair
import is.hail.io.vcf.VCFSettings
import is.hail.io.vcf.LoadVCF
import au.csiro.variantspark.hail.variant.phased.PhasedGenericRecordReader
import is.hail.io.vcf.GenericRecordReader
import au.csiro.variantspark.hail.variant.io.ExportVCFEx
import org.apache.spark.sql.Row
import au.csiro.variantspark.hail.variant.phased.BiCall


object FakeFamily {
  
  def createOffspringGeneric(v:Variant, genotypes:Iterable[Annotation]): Iterable[Annotation] = {
   
    // genotypes is iterable od row
    // create a random mixtue of parents
   
    val parentGenotypes = genotypes.toArray
    val motherGenotype = new BiCall(parentGenotypes(0).asInstanceOf[Row].getInt(0))
    val fatherGenotype = new BiCall(parentGenotypes(1).asInstanceOf[Row].getInt(0))
    
    
    // here come the randomisation
    val offspringGenotype = GTPair(motherGenotype(0), fatherGenotype(1))
    
    val offspringAndParent = Annotation(offspringGenotype.p) :: parentGenotypes.toList
    offspringAndParent
  } 
  
  def createOffspring(v:Variant, genotypes:Iterable[Genotype]): Iterable[Genotype] = {
    val gsb = new GenotypeStreamBuilder(v.nAlleles, isLinearScale = false)
    genotypes.foreach { gt => 
       gsb += gt
    }
    val gb = new GenotypeBuilder(v.nAlleles, false)
    gb.setGT(0)
    gsb.write(gb)
    gsb.result() 
  }  
}

class FakeFamilyTest extends SparkTest {

  @Test
  def testLoadPhased() {
    val hc = HailContext(sc)
    val vcf = hc.importVCFGenericEx("data/chr22_1000.vcf")
    println(vcf.count())
    vcf.exportVCFEx("target/genericPhasedO.vcf")
   }
   

  @Test
  def testProduceOffspring() {
    val hc = HailContext(sc)
    val vcf = hc.importVCFGenericEx("data/chr22_1000.vcf")
    println(vcf.count())
    val parents = vcf.filterSamplesList(Set("HG00096",	"HG00097"))
    print(parents.count())
   
    val sampleIds:List[String] = parents.sampleIds.toList.asInstanceOf[List[String]]
    val newIDS = "dsdsdsds" ::  sampleIds
    
    val transRdd = parents.rdd.mapPartitions(x => x.map { case (v, (a, g)) =>
        (v, (a,FakeFamily.createOffspringGeneric(v, g))) }, preservesPartitioning = true)  
        
    val offsrings = parents.copy(rdd =transRdd.asOrderedRDD, sampleIds = newIDS.toIndexedSeq, 
        sampleAnnotations =  Annotation.emptyIndexedSeq(newIDS.length))
    
    offsrings.exportVCFEx("target/phasedOffspring.vcf")
  }

}