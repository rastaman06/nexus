//
// $Id$

package com.threerings.nexus.gencode

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import java.util.{List => JList}
import javax.lang.model.element.{ExecutableElement, TypeElement, VariableElement}

/**
 * Contains metadata for a single `NexusService` interface.
 * @param elem the class's type element.
 */
class ServiceMetadata (val elem :TypeElement) extends Metadata {
  import ServiceMetadata._

  /** Returns the simple classname of this service. */
  def serviceName = elem.getSimpleName.toString

  /** Returns the imports needed by this class metadata. */
  def imports :Set[String] = Utils.collectImports(elem.asType) ++
    methods.flatMap(_.elem.getParameters.flatMap(p => Utils.collectImports(p.asType)))

  /** Returns all of the methods defined for this service. */
  def methods :JList[Method] = methodsBuf

  /** Adds a method to this metadata. Used when building. */
  def addMethod (elem :ExecutableElement) { methodsBuf += Method(elem) }
  private val methodsBuf = ListBuffer[Method]()

  override def toString () = String.format("[name=%s, methods=%s]", serviceName, methods)
}

object ServiceMetadata {
  case class Arg (elem :VariableElement, index :Int) {
    def name = elem.getSimpleName.toString
    def `type` = Utils.toString(elem.asType, true)
    def boxedType = Utils.toBoxedString(elem.asType, true)
    override def toString () = String.format("%s %s", `type`, name)
  }

  case class Method (elem :ExecutableElement) {
    val name = elem.getSimpleName.toString
    val args :JList[Arg] = elem.getParameters.zipWithIndex.map(Arg.tupled)
    override def toString () = String.format("%s(%s)", name, args.mkString(", "))
  }
}
