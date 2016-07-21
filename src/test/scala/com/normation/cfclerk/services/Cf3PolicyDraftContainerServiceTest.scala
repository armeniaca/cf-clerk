/*
*************************************************************************************
* Copyright 2011 Normation SAS
*************************************************************************************
*
* This file is part of Rudder.
*
* Rudder is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* In accordance with the terms of section 7 (7. Additional Terms.) of
* the GNU General Public License version 3, the copyright holders add
* the following Additional permissions:
* Notwithstanding to the terms of section 5 (5. Conveying Modified Source
* Versions) and 6 (6. Conveying Non-Source Forms.) of the GNU General
* Public License version 3, when you create a Related Module, this
* Related Module is not considered as a part of the work and may be
* distributed under the license agreement of your choice.
* A "Related Module" means a set of sources files including their
* documentation that, without modification of the Source Code, enables
* supplementary functions or services in addition to those offered by
* the Software.
*
* Rudder is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Rudder.  If not, see <http://www.gnu.org/licenses/>.

*
*************************************************************************************
*/

package com.normation.cfclerk.services

import org.junit.Test
import com.normation.cfclerk.domain._
import org.junit._
import org.junit.Assert._
import org.junit.runner.RunWith
import org.springframework.test.context.junit4._
import org.springframework.test.context._
import org.springframework.beans.factory.annotation._
import net.liftweb.common._

@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(Array("file:src/test/resources/spring-config-test.xml"))
class Cf3PolicyDraftContainerServiceTest {
  import scala.language.implicitConversions
  implicit def str2pId(id: String) = TechniqueId(TechniqueName(id), TechniqueVersion("1.0"))
  implicit def str2directiveId(id: String) = Cf3PolicyDraftId(id)

  @Autowired
  val containerService: Cf3PolicyDraftContainerService = null

  val identifier = "identifier"

  var policy1 : Cf3PolicyDraft = null

  var policy2 : Cf3PolicyDraft = null

  def newTechnique(id: TechniqueId) = Technique(id, "tech" + id, "", Seq(), Seq(), TrackerVariableSpec(), SectionSpec("plop"), None, Set(), None)

  def config() {

    val variable_1 = new InputVariable(InputVariableSpec("$variable1", "description"), Seq("var1"))
    val variable_2 = new InputVariable(InputVariableSpec("$variable2", "description"), Seq("var2"))
    val variable_22 = new InputVariable(InputVariableSpec("$variable22", "description"), Seq("var22"))

    policy1 = Cf3PolicyDraft(
        new Cf3PolicyDraftId("uuid1"),
        newTechnique(TechniqueId(TechniqueName("policy1"), TechniqueVersion("1.0"))),
        Map[String, Variable](variable_1.spec.name -> variable_1),
        TrackerVariable(TrackerVariableSpec(), Seq()),
        priority =0,
        serial = 0, order = List()
    )

    policy2 = Cf3PolicyDraft(
        new Cf3PolicyDraftId("uuid2"),
        newTechnique(TechniqueId(TechniqueName("policy2"), TechniqueVersion("1.0"))),
        Map[String, Variable](variable_2.spec.name -> variable_2, variable_22.spec.name -> variable_22),
        TrackerVariable(TrackerVariableSpec(), Seq()),
        priority =0,
        serial = 0, order = List()
    )


  }

  @Test
  def createContainer() {
    config()
    val container = containerService.createContainer(identifier, Set(), Seq(policy1))
    container match {
      case Full(x) =>
        assertEquals(x.outPath, identifier)
        assertEquals(x.getAll.size, 1)
        assert(x.get("uuid1") != None)

      case _ => fail("Couldn't create the container")
    }
  }

  @Test
  def createAndAddContainer() {
    config()
    val container = containerService.createContainer(identifier, Set(), Seq(policy1))

    container match {
      case Full(x) =>
        assertEquals(x.outPath, identifier)
        assertEquals(x.getAll.size, 1)
        assert(x.get("uuid1") != None)

      case _ => fail("Couldn't create the container")
    }

    containerService.addCf3PolicyDraft(container.openOrThrowException("I'm a failing test"), policy2)

    container match {
      case Full(x) =>
        assertEquals(x.outPath, identifier)
        assertEquals(x.getAll.size, 2)
        assert(x.get("uuid1") != None)
        assert(x.get("uuid2") != None)

      case _ => fail("Couldn't add policies")
    }
  }

}
