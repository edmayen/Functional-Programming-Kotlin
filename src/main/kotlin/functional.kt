package main.kotlin



import java.util.*
import kotlin.math.round

const val QUANTITY_DISCOUNT = 10
const val TAXES = 0.07

data class ShopUser(val id: String, val name: String, val lastName: String, val email: String )
data class Product(val code: String, val name: String, val quantityInventory: Int, val price: Double)

data class ProductBill(
    val code: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    var totalPrice: Double = 0.0
)

data class Bill(
    val timeStamp: String,
    val user: Unit,
    val products: List<ProductBill>,
    val total: Double
)

fun printRead(text: String): String
{
    print(text)
    return readLine() ?: ""
}

fun ShopUser.save(){
    UserManagement.shopUsers.add(this)
}

fun Product.save(){
    ProductManagement.products[this.code] = this
}

fun ProductBill.hasDiscount() = this.quantity > QUANTITY_DISCOUNT

object UserManagement{

    val shopUsers by lazy{
        mutableSetOf<ShopUser>()
    }

    fun createUser(block: ShopUser.() -> Unit): ShopUser {
        val id: String = printRead("Por favor ingresa tu numero de identificacion: ")
        val name: String = printRead("Por favor ingresa tu nombre: ")
        val lastName: String = printRead("Por favor ingresa tu apellido: ")
        val email: String = printRead("Por favor ingresa tu email: ")
        return ShopUser(id, name, lastName, email).apply(block)
    }

    fun listUser(block: (ShopUser) -> Unit){
        shopUsers.forEach { shopUser ->
            block(shopUser)
        }
    }

    fun getUserById(userId: String){
        shopUsers.first {
            it.id == userId
        }
    }

}

object ProductManagement{
    val products by lazy {
        mutableMapOf<String, Product>()
    }

    init {
        createProducts()
    }

    infix fun listProducts(block: (Product) -> Unit){
        products.forEach{ (_ , value) ->
            block(value)
        }
    }

    fun getProductByCode(code: String) = products[code]

    private fun product(block: ProductBuilder.() -> Unit) = ProductBuilder().apply(block).build().save()

    private fun createProducts(){

        product {
            code = "001"
            name = "Laptop"
            quantityInventory = 25
            price = 500.0
        }

        product {
            code = "002"
            name = "Smartphone"
            quantityInventory = 5
            price = 300.0
        }

        product {
            code = "003"
            name = "TV"
            quantityInventory = 35
            price = 700.0
        }

        product {
            code = "004"
            name = "Book"
            quantityInventory = 50
            price = 50.0
        }

    }

    class ProductBuilder {
        var code: String = ""
        var name: String = ""
        var quantityInventory: Int = 0
        var price: Double = 0.0

        fun build() = Product(code, name, quantityInventory, price)
    }

}

class BillManagement(private val userId: String) {

    companion object{
        const val DISCOUNT_VALUE = 0.5
    }

    private val buyProducts by lazy {
        mutableListOf<ProductBill>()
    }

    fun buyProduct(block: () -> Pair<String, Int>) {
        val dataBuyProduct = block()
        ProductManagement.getProductByCode(dataBuyProduct.first)?.let {
            buyProducts.add(
                ProductBill(
                    code = it.code,
                    name = it.name,
                    quantity = dataBuyProduct.second,
                    price = it.price
                )
            )
        }
    }

    fun printBill(){
        val productSequence = buyProducts.asSequence()

        val totalProducts = productSequence.map { productBill ->
            val totalPrice = productBill.price * productBill.quantity
            productBill.totalPrice = totalPrice
            if(productBill.hasDiscount()) {
                productBill.totalPrice = totalPrice - (totalPrice * DISCOUNT_VALUE)
            }
            productBill
        }.map { product ->
            product.totalPrice = round( product.totalPrice + (product.totalPrice * TAXES))
            product
        }.toList()

        Bill(
            timeStamp = Date().time.toString(),
            products = totalProducts,
            user = UserManagement.getUserById(userId),
            total = totalProducts.sumByDouble { it.totalPrice }
        ).also {
            print(it)
        }
    }

}

fun ShopUser.buyProducts(block: BillManagement.() -> Boolean): BillManagement{
    val billManagement = BillManagement(this.id)
    do {
        val haveMoreProducts: Boolean = billManagement.block()
    }while (haveMoreProducts)
    return billManagement
}

fun main(){
    val shopUser = UserManagement.createUser{
        save()
    }

    UserManagement.listUser{
        println("Este es un usurio del sistema pos => $it")
    }

    ProductManagement listProducts {
        println(it)
    }

    shopUser.buyProducts {
        buyProduct {
            val codeProduct = printRead("Ingrese el numero del prodeucto: ")
            val quantity = printRead("Ingrese la cantidad: ")
            Pair(codeProduct, quantity.toInt())
        }
        printRead("Desea comprar mas productos (S/N)") == "S"
    }.printBill()

}


