    flowchart TB

        Client[Customer / Admin]
    
        Gateway[API Gateway]
    
        Auth[Auth Service]
        Product[Product Service]
        Cart[Cart Service]
        Inventory[Inventory Service]
        Order[Order Service]
        Payment[Payment Service]
    
        Kafka[(Apache Kafka)]
    
        Notification[Notification Service]
        Analytics[Analytics Service]
    
        Client --> Gateway
    
        Gateway --> Auth
        Gateway --> Product
        Gateway --> Cart
        Gateway --> Inventory
        Gateway --> Order
        Gateway --> Payment
    
        Order --> Inventory
        Order --> Payment
    
        Order --> Kafka
        Inventory --> Kafka
        Payment --> Kafka
    
        Kafka --> Notification
        Kafka --> Analytics