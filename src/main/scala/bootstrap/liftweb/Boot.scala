/*
 * Copyright 2007-2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package bootstrap.liftweb

import _root_.net.liftweb._
import util.{Helpers, Box, Full, Empty, Failure, Log, NamedPF, Props}
import http._
import sitemap._
import Helpers._

import com.jcraft.lift.model._

import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}

import _root_.java.sql.{Connection, DriverManager}
import _root_.javax.servlet.http.{HttpServlet, HttpServletRequest , HttpServletResponse, HttpSession}
import _root_.scala.actors._
import Actor._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    LiftRules.addToPackages("com.jcrat.lift")


    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
    Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
    Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    LiftRules.setSiteMap(SiteMap(MenuInfo.menu :_*))

  }

  private def makeUtf8(req: HttpServletRequest): Unit = {req.setCharacterEncoding("UTF-8")}
}

object MenuInfo {
  import Loc._

  def menu: List[Menu] =  Menu(Loc("home", List("index"), "Home")) ::
  Menu(Loc("jpademo_top", List("jpademo", "index"), "JPADemo"),
       Menu(Loc("Authors", List("jpademo", "authors", "list"), "Author List")),
       Menu(Loc("Add Authors", List("jpademo", "authors", "add"), "Add Author", Hidden)),
       Menu(Loc("Books", List("jpademo", "books", "list"), "Book List")),
       Menu(Loc("Add Books", List("jpademo", "books", "add"), "Add Book", Hidden)),
       Menu(Loc("Book Search", List("jpademo", "books", "search"), "Book Search"))
  ) ::
  Nil
}
