package com.zuchos.pizzeria

import com.zuchos.pizzeria.PizzaOrderingService.Composition.PredefinedComposition
import com.zuchos.pizzeria.PizzaOrderingService.{Composition, CompositionId, Crust, Size}
import com.zuchos.pizzeria.PizzaOrderingService.OrderedItem.Pizza
import org.scalatest.{EitherValues, FlatSpec, Matchers}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}

class PizzaOrderingServiceSpec extends FlatSpec with ScalaFutures with EitherValues with Eventually with Matchers with IntegrationPatience {

  behavior of classOf[PizzaOrderingService].getSimpleName

  it should "process order" in {
    //Given
    val userService: UserService = new UserService()
    val usersHomeAddress         = Address("Elm Street", "1/22B", "Warsaw")
    val userId =
      userService.registerUser("Lukasz", usersHomeAddress, "+48-555-666-777", "testemail@zuchos.com").futureValue

    val provider = new TestPaymentProvider
    provider.addFunds(userId, 200)

    val paymentService        = new PaymentService(provider)
    val receiptHistoryService = new ReceiptHistoryService()

    val pizzaDeliveryService = new PizzaDeliveryService()
    val kitchen              = new Kitchen
    val service = new PizzaOrderingService(
      userService,
      paymentService,
      kitchen,
      pizzaDeliveryService,
      receiptHistoryService
    )

    //When
    val orderedItems = List(Pizza(Crust.CheeseFilledCrust, Size.XL, PredefinedComposition(CompositionId.Havaian, List.empty)))
    val Right(receipt) = service
      .orderPizza(
        userId,
        None,
        orderedItems
      )
      .futureValue

    //Then
    receipt.items shouldBe orderedItems
    receipt.price shouldBe BigDecimal(14.0)
    receipt.deliveryAddress shouldBe usersHomeAddress

    provider.getUserFunds(userId).futureValue shouldBe (BigDecimal(200.00 - 14.00))

    val usersReceipts = receiptHistoryService.find(userId).futureValue

    usersReceipts should contain only (receipt)

    pizzaDeliveryService.getScheduledDeliveries.futureValue should contain only ((receipt.orderId, usersHomeAddress))

    eventually {
      kitchen.getPreparedOrders.futureValue should contain only (receipt.orderId)
      pizzaDeliveryService.getDelivered.futureValue should contain only (receipt.orderId)
    }
  }
}
