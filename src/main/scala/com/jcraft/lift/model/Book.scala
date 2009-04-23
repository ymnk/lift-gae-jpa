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

package com.jcraft.lift.model

import com.google.appengine.api.datastore.Key

import java.util.Date
import javax.persistence._
import org.hibernate.annotations.Type

/**
 This class represents a book that we might want to read.
*/
@Entity
class Book {
  @Id
  @GeneratedValue(){val strategy = GenerationType.IDENTITY}
  var id : Key = _

  @Column{val nullable = false}
  var title : String = ""

  @Column{val nullable = true}
  var published : Date = new Date()

  @Column{val nullable = true}
  var genre : String = "unknown" //Genre.unknown.toString

  @Column{val nullable = false}
  @ManyToOne
  var author : Author = _
}
