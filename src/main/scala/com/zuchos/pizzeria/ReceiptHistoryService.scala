package com.zuchos.pizzeria

import com.zuchos.pizzeria.UserService.UserId

import scala.collection.mutable
import scala.concurrent.Future

class ReceiptHistoryService {

  private val receipts: mutable.Map[UserId, OrderReceipt] = new mutable.HashMap()

  def addToHistory(userId: UserId, orderReceipt: OrderReceipt): Future[Unit] = {
    receipts.put(userId, orderReceipt)
    Future.unit
  }

  def findAll:Future[List[OrderReceipt]] = {
    Future.successful(receipts.values.toList)
  }
}
