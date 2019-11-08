package com.zuchos.pizzeria

import java.time.LocalDateTime

import com.zuchos.pizzeria.PizzaOrderingService.OrderId

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Random

class PizzaDeliveryService {
  private val scheduledDeliveries: mutable.ListBuffer[(OrderId, Address, LocalDateTime)] = new mutable.ListBuffer()

  def scheduleDelivery(
      orderId: OrderId,
      estimatedReadyTime: LocalDateTime,
      address: Address,
      items: List[PizzaOrderingService.OrderedItem]
  ): Future[LocalDateTime] = {
    val estimatedDeliveryTime = estimatedReadyTime.plusSeconds(Random.between(1, 5))
    val actualDeliveryTime    = estimatedDeliveryTime.plusSeconds(Random.between(2, 4))
    scheduledDeliveries.addOne((orderId, address, actualDeliveryTime))
    Future.successful(estimatedDeliveryTime)
  }

  def getScheduledDeliveries: Future[List[(OrderId, Address)]] = {
    Future.successful(scheduledDeliveries.map(p => (p._1, p._2)).toList)
  }

  def getDelivered: Future[List[OrderId]] = {
    val now = LocalDateTime.now()
    Future.successful(scheduledDeliveries.filter(_._3.isBefore(now)).map(_._1).toList)
  }
}
