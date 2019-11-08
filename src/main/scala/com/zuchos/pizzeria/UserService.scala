package com.zuchos.pizzeria

import java.util.UUID

import scala.concurrent.Future
import UserService._

import scala.collection.mutable

case class Address(line1: String = "", line2: String = "", city: String = "")

case class User(userId: UserId, name: String, address: Address, phoneNumber: String, email: String)

class UserService {

  private val userMap: mutable.Map[UserId, User] = new mutable.HashMap()

  def registerUser(name: String, address: Address, phoneNumber: String, email: String): Future[UserId] = {
    val userId = UUID.randomUUID()
    userMap.addOne(userId, User(userId, name, address, phoneNumber, email))
    Future.successful(userId)
  }

  def getUser(userId: UserId): Future[User] = Future.successful(userMap(userId))

}

object UserService {
  type UserId = UUID
}
