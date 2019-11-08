package com.zuchos.pizzeria

import com.zuchos.pizzeria.UserService.UserId

import scala.collection.mutable
import scala.concurrent.Future

trait PaymentProvider {
  def charge(userId: UserId, title: String, price: BigDecimal): Future[Boolean]
}

class TestPaymentProvider extends PaymentProvider {
  private val funds: mutable.Map[UserId, BigDecimal] = new mutable.HashMap()

  def addFunds(userId: UserId, amount: BigDecimal): Unit = {
    val updatedUsersFunds = funds.get(userId).map(current => current + amount).getOrElse(amount)
    funds.put(userId, updatedUsersFunds)
  }

  override def charge(userId: UserId, title: String, price: BigDecimal): Future[Boolean] = {
    val currentAmount = funds.get(userId)
    val canAfford     = currentAmount.find(amount => amount >= price)
    canAfford.foreach { amount =>
      funds.put(userId, amount - price)
    }
    Future.successful(canAfford.isDefined)
  }

  def getUserFunds(userId: UserId): Future[BigDecimal] = Future.successful(funds(userId))
}
