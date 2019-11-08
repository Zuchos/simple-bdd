package com.zuchos.pizzeria

import com.zuchos.pizzeria.PizzaOrderingService.Composition.{CustomerComposition, PredefinedComposition}
import com.zuchos.pizzeria.PizzaOrderingService.Crust.Crust
import com.zuchos.pizzeria.PizzaOrderingService.{CompositionId, Crust, OrderId, Size}
import com.zuchos.pizzeria.PizzaOrderingService.OrderedItem.{Coke, Pizza}
import com.zuchos.pizzeria.PizzaOrderingService.Size.Size
import com.zuchos.pizzeria.UserService.UserId

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentService(billingService: PaymentProvider) {
  def payForOrder(userId: UserId, orderId: OrderId, items: List[PizzaOrderingService.OrderedItem]): Future[BigDecimal] = {
    val price = items
      .map {
        case pizza: Pizza => PriceCalculator.calculate(pizza)
        case coke: Coke   => PriceCalculator.calculate(coke)
      }
      .iterator
      .sum
    billingService.charge(userId, s"Payment for order with id$orderId", price).map {
      case true  => price
      case false => throw new RuntimeException("Payment failure!")
    }
  }
}

object PriceCalculator {
  def calculate(coke: Coke): BigDecimal = {
    calculateBySize(coke.size)
  }
  def calculate(pizza: Pizza): BigDecimal = {
    calculateByCrust(pizza.crust) + calculateBySize(pizza.size) + calculate(pizza.composition)
  }

  private def calculateByCrust(crust: Crust): BigDecimal = crust match {
    case Crust.ThinCrust         => BigDecimal(5.0)
    case Crust.ThickCrust        => BigDecimal(6.0)
    case Crust.CheeseFilledCrust => BigDecimal(7.0)
  }

  private def calculateBySize(size: Size): BigDecimal = size match {
    case Size.S  => BigDecimal(1.0)
    case Size.M  => BigDecimal(2.0)
    case Size.L  => BigDecimal(3.0)
    case Size.XL => BigDecimal(4.0)
  }

  private def calculate(composition: PizzaOrderingService.Composition): BigDecimal = composition match {
    case CustomerComposition(ingredients)      => BigDecimal(ingredients.size)
    case PredefinedComposition(menuId, extras) => calculate(menuId) + BigDecimal(extras.size)
  }

  private def calculate(compositionId: CompositionId): BigDecimal = {
    BigDecimal(3.0)
  }
}
