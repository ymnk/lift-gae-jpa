/*
 * Copyright 2008 WorldWide Conferencing, LLC
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
package com.jcraft.lift.snippet

import scala.xml.{NodeSeq,Text}

import net.liftweb.http.{RequestVar,S,SHtml}
import net.liftweb.util.{Helpers,Log}
import S._
import Helpers._

import com.jcraft.lift.model._
import com.jcraft.lift.model.Model._

import javax.persistence.{EntityExistsException,PersistenceException}

class AuthorOps {
  def list (xhtml : NodeSeq) : NodeSeq = {
    val authors = Model.createNamedQuery[Author]("findAllAuthors").getResultList()
    def findBooksByAuthor(a:Author)={
     Model.createNamedQuery[Book]("findAllBooks").getResultList().toList.filter( b => b.author.id == a.id)
    }

    authors.flatMap(author =>
      bind("author", xhtml,
	   "name" -> Text(author.name),
	   "count" -> SHtml.link("/jpademo/books/search", {() =>
	     BookOps.resultVar(findBooksByAuthor(author))
	     }, Text(author.books.size().toString)),
	   "edit" -> SHtml.link("add.html", () => authorVar(author), Text(?("Edit")))))
  }

  // Set up a requestVar to track the author object for edits and adds
  object authorVar extends RequestVar(new Author())
  lazy val author = authorVar.is

  def add (xhtml : NodeSeq) : NodeSeq = {
    def doAdd () = {
      if (author.name.length == 0) {
	error("emptyAuthor", "The author's name cannot be blank")
      } else {
	try {
	  Model.mergeAndFlush(author)
	  redirectTo("list.html")
	} catch {
	  case ee : EntityExistsException => 
            error("That author already exists.")
	  case pe : PersistenceException => 
            error("Error adding author"); Log.error("Author add failed", pe)
	}
      }
    }

    bind("author", xhtml,
	 "name" -> SHtml.text(author.name, author.name=_),
	 "submit" -> SHtml.submit(?("Save"), doAdd))
  }
}
