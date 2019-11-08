package com.zuchos.pizzeria

import java.time.LocalDateTime
import java.util.UUID

import com.zuchos.pizzeria.PizzaOrderingService.Crust.Crust
import com.zuchos.pizzeria.PizzaOrderingService.Size.Size
import com.zuchos.pizzeria.UserService.UserId

import scala.concurrent.Future
import com.zuchos.pizzeria.PizzaOrderingService.{OrderId, _}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class Kitchen {
  private val preparedOrders: mutable.ListBuffer[(OrderId, LocalDateTime)] = new mutable.ListBuffer()

  def startPreparingOrder(orderId: OrderId, items: List[PizzaOrderingService.OrderedItem]): LocalDateTime = {
    val orderedAt          = LocalDateTime.now()
    val estimatedReadyTime = orderedAt.plusSeconds(Random.between(1, 5))
    val actualReadyTime    = orderedAt.plusSeconds(Random.between(1, 3))
    preparedOrders.addOne((orderId, actualReadyTime))
    estimatedReadyTime
  }

  def getPreparedOrders: Future[List[OrderId]] = {
    val now = LocalDateTime.now()
    Future.successful(preparedOrders.filter(_._2.isBefore(now)).map(_._1).toList)
  }

}

sealed trait PizzaServiceError
case class OrderReceipt(
    orderId: OrderId,
    price: BigDecimal,
    items: List[OrderedItem],
    deliveryAddress: Address,
    estimatedDeliveryTime: LocalDateTime
)

class PizzaOrderingService(
    userService: UserService,
    paymentService: PaymentService,
    kitchen: Kitchen,
    pizzaDeliveryService: PizzaDeliveryService,
    receiptHistoryService: ReceiptHistoryService
) {
  def orderPizza(userId: UserId, address: Option[Address], items: List[OrderedItem]): Future[Either[PizzaServiceError, OrderReceipt]] = {
    val orderId = UUID.randomUUID()
    for {
      user               <- userService.getUser(userId)
      price              <- paymentService.payForOrder(user.userId, orderId, items)
      estimatedReadyTime <- Future.successful(kitchen.startPreparingOrder(orderId, items))
      deliveryAddress = address.getOrElse(user.address)
      estimatedDeliveryTime <- pizzaDeliveryService.scheduleDelivery(orderId, estimatedReadyTime, deliveryAddress, items)
      receipt = OrderReceipt(orderId, price, items, deliveryAddress, estimatedDeliveryTime)
      _ <- receiptHistoryService.addToHistory(userId, receipt)
    } yield {
      Right(receipt)
    }
  }
}

object PizzaOrderingService {
  type OrderId = UUID

  type CompositionId = Int

  object CompositionId {

    val Margarita       = 1
    val QuattroStagioni = 2
    val Havaian         = 3

    def apply(compositionName: String): CompositionId = compositionName match {
      case "Margarita"        => Margarita
      case "Quattro Stagioni" => QuattroStagioni
      case "Havaian"          => Havaian
    }
  }

  type IngredientId = Int

  object IngredientId {
    val Ham         = 1
    val Cheese      = 2
    val TomatoSauce = 3
    val Garlic      = 4
    val Jalapeno    = 5

    def apply(ingredientName: String): IngredientId = ingredientName match {
      case "Ham"          => Ham
      case "Cheese"       => Cheese
      case "Tomato Sauce" => TomatoSauce
      case "Garlic"       => Garlic
      case "Jalapeno"     => Jalapeno
    }
  }

  sealed trait OrderedItem

  object Crust extends Enumeration {
    type Crust = Value
    val ThinCrust, ThickCrust, CheeseFilledCrust = Value
  }

  object Size extends Enumeration {
    type Size = Value
    val S, M, L, XL = Value
  }

  sealed trait Composition
  object Composition {
    case class PredefinedComposition(compositionId: CompositionId, extras: List[IngredientId]) extends Composition
    case class CustomerComposition(ingredients: List[IngredientId])                            extends Composition
  }

  object OrderedItem {

    case class Pizza(crust: Crust, size: Size, composition: Composition) extends OrderedItem
    case class Coke(size: Size)                                          extends OrderedItem

  }

}
