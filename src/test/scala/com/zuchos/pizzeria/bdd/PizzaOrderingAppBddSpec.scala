package com.zuchos.pizzeria.bdd

import com.typesafe.config.ConfigFactory
import com.zuchos.pizzeria.PizzaOrderingService.Composition.{CustomerComposition, PredefinedComposition}
import com.zuchos.pizzeria.PizzaOrderingService.{Composition, CompositionId, Crust, IngredientId, OrderedItem, Size}
import com.zuchos.pizzeria.UserService.UserId
import com.zuchos.pizzeria.{PizzeriaModule, TestPaymentProvider}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, FlatSpecLike, Matchers}

import scala.collection.mutable


class PizzaOrderingAppBddSpec extends FlatSpecLike with Matchers with ScalaFutures with Eventually with EitherValues  with IntegrationPatience {

  val simpleScenarios = List("OrderMargarita.conf", "OrderHavaiianWithExtraJalapeno.conf").map(ConfigFactory.load)

  simpleScenarios.foreach {
    scenarioConfig =>
      test(new Scenario(scenarioConfig))
  }


  def test(scenario: Scenario): Unit = {
    //for later clean up
    val testPaymentProvider = new TestPaymentProvider()
    val pizzeriaModule = new PizzeriaModule(testPaymentProvider)
    val userByName = new mutable.HashMap[String, UserId]()

    it should s"execute scenario:\n ${scenario.name}" in {
      // given
      scenario.users.foreach {
        givenUser =>
          withClue(s"Add user $givenUser") {
            val userId = pizzeriaModule.userService.registerUser(givenUser.name, givenUser.address, "+48 111 111 111", "test@the.west").futureValue
            userByName.addOne(givenUser.name, userId)
          }
      }

      scenario.funds.foreach {
        userFunds =>
          withClue(s"Add user funds $userFunds") {
            testPaymentProvider.addFunds(userByName(userFunds.user), userFunds.funds)
          }
      }

      //when
      val receipts = scenario.makeOrders.map {
        makeOrder =>
          (makeOrder.orderName, pizzeriaModule.pizzaOrderingService.orderPizza(userByName(makeOrder.user), makeOrder.deliveryAddress, toOrderedItems(makeOrder.items)).futureValue.right.value)
      }.toMap

      def getReceiptByName(name:String) = receipts.getOrElse(name, fail(s"No receipt for name:$name"))

      //then
      scenario.expectedReceipts.map {
        expectedReceipt =>
          val actual = getReceiptByName(expectedReceipt.orderName)
          withClue(s"Asserting expected receipt, expected:$expectedReceipt, actual:$actual \n") {
            actual.deliveryAddress shouldBe expectedReceipt.deliveryAddress
            actual.price shouldBe expectedReceipt.price
          }
      }


      if (scenario.expectedFunds.nonEmpty) {
        scenario.expectedFunds.foreach {
          expectedFunds =>
            withClue(s"Asserting expected funds of user ${expectedFunds.user} \n") {
              testPaymentProvider.getUserFunds(userByName(expectedFunds.user)).futureValue shouldBe expectedFunds.funds
            }
        }
      }

      if (scenario.receiptsHistoryEntries.nonEmpty) {
        withClue(s"Asserting expected receipts \n") {
          pizzeriaModule.receiptHistoryService.findAll.futureValue should contain allElementsOf (scenario.receiptsHistoryEntries.map(getReceiptByName))
        }
      }

      if (scenario.scheduledDeliveries.nonEmpty) {
        withClue(s"Asserting expected scheduled deliveries \n") {
          pizzeriaModule.pizzaDeliveryService.getScheduledDeliveries.futureValue should contain allElementsOf (
            scenario.scheduledDeliveries.map(scheduledDelivery => (getReceiptByName(scheduledDelivery.orderName).orderId, getReceiptByName(scheduledDelivery.orderName).deliveryAddress))
            )
        }
      }

      eventually {
        if (scenario.scheduledDeliveries.nonEmpty) {
          withClue(s"Asserting expected scheduled deliveries \n") {
            pizzeriaModule.pizzaDeliveryService.getDelivered.futureValue should contain allElementsOf (scenario.scheduledDeliveries.map(d => getReceiptByName(d.orderName).orderId))
          }
          withClue(s"Asserting expected prepared orders \n") {
            pizzeriaModule.kitchen.getPreparedOrders.futureValue should contain allElementsOf (scenario.scheduledDeliveries.map(d => getReceiptByName(d.orderName).orderId))
          }
        }
      }
    }
  }

  private def toOrderedItems(items: OrderItems): List[OrderedItem] = {
    items.pizzas.map {
      pizza =>
        val composition: Composition = pizza.composition match {
          case "custom" => CustomerComposition(pizza.extraIngredients.map(IngredientId(_)))
          case predefined => PredefinedComposition(CompositionId(predefined), pizza.extraIngredients.map(IngredientId(_)))
        }
        OrderedItem.Pizza(Crust.withName(pizza.crust), Size.withName(pizza.size), composition)
    }
  }
}
