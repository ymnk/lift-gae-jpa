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

import _root_.java.text.{ParseException,SimpleDateFormat}

import _root_.scala.xml.{NodeSeq,Text}

import _root_.net.liftweb.http.{RequestVar,S,SHtml}
import _root_.net.liftweb.util.{Box,Empty,Full,Helpers,Log}
import S._
import Helpers._

import com.jcraft.lift.model._
import com.jcraft.lift.model.Model._

import _root_.javax.persistence.{EntityExistsException,PersistenceException}

// Make an object so that other pages can access (ie Authors)
object BookOps {
  // Object to hold search results
  object resultVar extends RequestVar[List[Book]](Nil)
}

class BookOps {
  val formatter = new java.text.SimpleDateFormat("yyyyMMdd")

  def list (xhtml : NodeSeq) : NodeSeq = {
    val books = Model.createNamedQuery[Book]("findAllBooks").getResultList()

    def findAuthor(a:Author)={
     Model.createQuery[Author]("select from com.jcraft.lift.model.Author a where a.id = :id").setParameter("id", a.id).findOne
    }

    books.flatMap(book =>
      bind("book", xhtml,
	   "title" -> Text(book.title),
	   "published" -> Text(formatter.format(book.published)),
	   "genre" -> Text(if (book.genre != null) book.genre.toString else ""),
	   "author" -> Text(findAuthor(book.author).get.name),
	   "edit" -> SHtml.link("add.html", () => bookVar(book), Text(?("Edit")))))
  }

  // Set up a requestVar to track the book object for edits and adds
  object bookVar extends RequestVar(new Book())
  def book = bookVar.is

  // Utility methods for processing a submitted form
  def is_valid_Book_? (toCheck : Book) : Boolean =
    List((if (toCheck.title.length == 0) { S.error("You must provide a title"); false } else true),
	 (if (toCheck.published == null) { S.error("You must provide a publish date"); false } else true),
	 (if (toCheck.genre == null) { S.error("You must select a genre"); false } else true),
	 (if (toCheck.author == null) { S.error("You must select an author"); false } else true)).forall(_ == true)

  def setDate (input : String, toSet : Book) {
    try {
      toSet.published=formatter.parse(input)
    } catch {
      case pe : ParseException => S.error("Error parsing the date")
    }
  }

  // The add snippet method
  def add (xhtml : NodeSeq) : NodeSeq = {
    def doAdd () = 
      if (is_valid_Book_?(book)) {
	try{
	  book.author = Model.find[Author](classOf[Author], book.author.id).get
	  Model.mergeAndFlush(book)
	  redirectTo("list")
	} catch {
	  case ee : EntityExistsException => error("That book already exists.")
	  case pe : PersistenceException => error("Error adding book"); Log.error("Book add failed", pe)
	}
      }


    // Hold a val here so that the closure holds it when we re-enter this method
    val current = book

    val authors = Model.createNamedQuery[Author]("findAllAuthors").getResultList()
    val choices = authors.map(author => (author.id.toString -> author.name)).toList
    val default = if (book.author != null) { Full(book.author.id.toString) } else { Empty }

    def find(n:String):Author={
       val l = for(a<-authors if a.id.toString==n) yield  a
      l(0)
    }

    bind("book", xhtml,
	 "id" -> SHtml.hidden(() => bookVar(current)),
	 "title" -> SHtml.text(book.title, book.title=_),
	 "published" -> SHtml.text(formatter.format(book.published), setDate(_, book)) % ("id" -> "published"),
	 "genre" -> SHtml.select(Genre.getNameDescriptionList, (Box.legacyNullTest(book.genre).map(_.toString) or Full("")), choice => book.genre= Genre.valueOf(choice).getOrElse(null).toString),
	 "author" -> SHtml.select(choices, default, {authId:String => book.author=Model.getReference(classOf[Author], find(authId).id)}),
	 "save" -> SHtml.submit(?("Save"), doAdd))
  }

  def searchResults (xhtml : NodeSeq) : NodeSeq = BookOps.resultVar.is.flatMap(result =>
    bind("result", xhtml, "title" -> Text(result.title), "author" -> Text(result.author.name)))

  def search (xhtml : NodeSeq) : NodeSeq = {
    var title = ""

    def doSearch () = {
      BookOps.resultVar(Model.createNamedQuery[Book]("findBooksByTitle", "title" -> title.toLowerCase).getResultList().toList)
    }

    bind("search", xhtml,
	 "title" -> SHtml.text(title, title = _),
	 "run" -> SHtml.submit(?("Search"), doSearch _))
  }
}
