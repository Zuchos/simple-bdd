package com.zuchos.pizzeria.bdd

import com.typesafe.config.Config
import com.zuchos.pizzeria._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.namemappers.implicits.hyphenCase

final class Scenario(scenarioConfig: Config) {
  val name: String = scenarioConfig.getString("scenario-name")
  //Given
  val users: List[GivenUser] = scenarioConfig.as[List[GivenUser]]("given.users")
  val funds: List[GivenUserFunds] = scenarioConfig.as[List[GivenUserFunds]]("given.funds")

  //When
  val makeOrders: List[WhenMakeOrder] = scenarioConfig.as[List[WhenMakeOrder]]("when.make-orders")

  //Then
  val expectedReceipts: List[ExpectedReceipt] = scenarioConfig.as[List[ExpectedReceipt]]("then.users-receipts")
  val expectedFunds: List[ExpectedUserFunds] = scenarioConfig.as[List[ExpectedUserFunds]]("then.user-funds")
  val receiptsHistoryEntries: List[String] = scenarioConfig.as[List[String]]("then.receipts-history")
  val scheduledDeliveries: List[ExpectedScheduledDeliveries] = scenarioConfig.as[List[ExpectedScheduledDeliveries]]("then.scheduled-deliveries")
  val deliveredOrders: List[String] = scenarioConfig.as[List[String]]("then.delivered-orders")
  val kitchenPreparedOrders: List[String] = scenarioConfig.as[List[String]]("then.kitchen-has-prepared-orders")
}

case class GivenUserFunds(user: String, funds: BigDecimal)

case class GivenUser(name: String, address: Address)

case class WhenMakeOrder(orderName: String, user: String, items: OrderItems, deliveryAddress: Option[Address] = None)

case class OrderItems(pizzas: List[Pizza])

case class Pizza(composition: String, crust: String, size: String, extraIngredients: List[String]= List.empty)

case class ExpectedReceipt(orderName: String, price: BigDecimal, deliveryAddress: Address)

case class ExpectedUserFunds(user: String, funds: BigDecimal)

case class ExpectedScheduledDeliveries(orderName: String, deliveryAddress: Address)