package com.zuchos.pizzeria

class PizzeriaModule(paymentProvider:PaymentProvider) {
  val userService: UserService = new UserService()
  val paymentService        = new PaymentService(paymentProvider)
  val receiptHistoryService = new ReceiptHistoryService()
  val pizzaDeliveryService = new PizzaDeliveryService()
  val kitchen              = new Kitchen
  val pizzaOrderingService = new PizzaOrderingService(
    userService,
    paymentService,
    kitchen,
    pizzaDeliveryService,
    receiptHistoryService
  )
}
