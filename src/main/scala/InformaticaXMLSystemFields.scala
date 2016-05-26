/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import scala.xml._
import scala.xml.transform._
import scala.xml.Elem
import scala.xml.factory.XMLLoader
import java.io._

object InformaticaXMLSystemFields {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    val f = javax.xml.parsers.SAXParserFactory.newInstance()

    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

    val p = f.newSAXParser()

    val xml = scala.xml.XML.withSAXParser(p).load(args(0))

    object MainConnectionTransform extends RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case <MAPPING>{therms @ _*}</MAPPING> => {
            val ccd = "\"" + args(1) + "\""
            therms.take(therms.size-1) ++ 
                   <MAPPINGVARIABLE DATATYPE ="string" DEFAULTVALUE ={ ccd } DESCRIPTION ="" ISEXPRESSIONVARIABLE ="NO" ISPARAM ="YES" NAME ="$$mpSourceSystemCCD" PRECISION ="3" SCALE ="0" USERDEFINED ="YES"/> ++ 
                   <MAPPINGVARIABLE DATATYPE ="integer" DEFAULTVALUE ="" DESCRIPTION ="" ISEXPRESSIONVARIABLE ="NO" ISPARAM ="YES" NAME ="$$spDwhJobID" PRECISION ="10" SCALE ="0" USERDEFINED ="YES"/> ++
                   <MAPPINGVARIABLE DATATYPE ="date/time" DEFAULTVALUE ="" DESCRIPTION ="" ISEXPRESSIONVARIABLE ="NO" ISPARAM ="YES" NAME ="$$mpEffectiveFrom" PRECISION ="29" SCALE ="9" USERDEFINED ="YES"/> ++
                   <MAPPINGVARIABLE DATATYPE ="date/time" DEFAULTVALUE ="" DESCRIPTION ="" ISEXPRESSIONVARIABLE ="NO" ISPARAM ="YES" NAME ="$$mpEffectiveTo" PRECISION ="29" SCALE ="9" USERDEFINED ="YES"/> ++
                   therms(therms.size-1)
        }
        case sessfl @ <TRANSFORMATION>{trans @ _*}</TRANSFORMATION> if {
            val attrs = sessfl.attributes.asAttrMap
            attrs("NAME").toLowerCase.contains("fields")
          } => {
            trans.filter(_.label == "TRANSFORMFIELD") ++
              <TRANSFORMFIELD DATATYPE ="string" DEFAULTVALUE ="" DESCRIPTION ="" EXPRESSION ="$$mpSourceSystemCCD" EXPRESSIONTYPE ="GENERAL" NAME ="SOURCE_SYSTEM_CCD" PICTURETEXT ="" PORTTYPE ="OUTPUT" PRECISION ="3" SCALE ="0"/> ++
            <TRANSFORMFIELD DATATYPE ="date/time" DEFAULTVALUE ="" DESCRIPTION ="" EXPRESSION ="SYSTIMESTAMP()" EXPRESSIONTYPE ="GENERAL" NAME ="PROCESSED_DTTM" PICTURETEXT ="" PORTTYPE ="OUTPUT" PRECISION ="29" SCALE ="9"/> ++
            <TRANSFORMFIELD DATATYPE ="string" DEFAULTVALUE ="" DESCRIPTION ="" EXPRESSION ="&apos;N&apos;" EXPRESSIONTYPE ="GENERAL" NAME ="DELETED_FLAG" PICTURETEXT ="" PORTTYPE ="OUTPUT" PRECISION ="1" SCALE ="0"/> ++
            <TRANSFORMFIELD DATATYPE ="double" DEFAULTVALUE ="" DESCRIPTION ="" EXPRESSION ="$$spDWHJobID" EXPRESSIONTYPE ="GENERAL" NAME ="DWH_JOB_ID" PICTURETEXT ="" PORTTYPE ="OUTPUT" PRECISION ="15" SCALE ="0"/> ++
            trans.filter(_.label != "TRANSFORMFIELD")
        }
        case other => other
      }
    }

    object RuleMainConnectionTransoform extends RuleTransformer(MainConnectionTransform)

    val newXml = RuleMainConnectionTransoform(xml)

    val ddd = new scala.xml.dtd.DocType("POWERMART",
                                        scala.xml.dtd.SystemID("powrmart.dtd"),
                                        Nil)
    
    scala.xml.XML.save("testxmld.xml", newXml, "windows-1251", true, ddd)
  }

}
