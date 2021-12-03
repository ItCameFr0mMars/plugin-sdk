import com.google.gson.Gson
import com.lambda.client.LambdaMod
import com.lambda.client.util.TickTimer
import com.lambda.client.util.TimeUnit
import com.lambda.client.util.WebUtils
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.plugin.api.PluginLabelHud
import com.lambda.client.util.threads.defaultScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

internal object StocksHud: PluginLabelHud(
    name = "Stocks",
    category = Category.MISC,
    description = "Live updating stock price",
    pluginMain = PluginStocks
) {
    private var symbol by setting("Symbol", "TSLA")
    private val tickdelay by setting("Delay", 30, 5..120, 1)
    private var token by setting("Token", "Set your token with the command ;set Stocks Token (token)")
    private val ticktimer = TickTimer(TimeUnit.SECONDS)
    private var url = "https://finnhub.io/api/v1/quote?symbol=$symbol&token=$token"
    private var stockData = StockData(0.0)
    private var price = 0.0
    private var prevprice = 0.0
    private var sentwarning = false
    private var color = secondaryColor


    override fun SafeClientEvent.updateText() {
        if (!sentwarning) {
            sendWarning()
        }

        if (ticktimer.tick(tickdelay) && !token.startsWith("Set")) {
            defaultScope.launch {
                try {
                    url = "https://finnhub.io/api/v1/quote?symbol=${symbol.uppercase()}&token=$token"
                    price = Gson().fromJson(WebUtils.getUrlContents(url), StockData::class.java).c
                } catch (e: Exception) {
                    LambdaMod.LOG.error("Failed to connect to finnhub api", e)
                    MessageSendHelper.sendWarningMessage("[StockTicker] Token not accepted: $token")
                }
            }
        }
        displayText.clear()
        displayText.add("Current Price of ${symbol.uppercase()} is", primaryColor)
        if (prevprice < price) {
            color.r = 0
            color.g = 255
            color.b = 0
            color.a = 255
            displayText.add("$price", color)
        } else if (prevprice > price ) {
            color.r = 255
            color.g = 0
            color.b = 0
            color.a = 255
            displayText.add("$price", color)
        } else {
            displayText.add("$price", secondaryColor)
        }; if (ticktimer.tick(2)) {
            displayText.add("$price", secondaryColor)
            prevprice = price
        }
    }


    private fun sendWarning() {
        MessageSendHelper.sendWarningMessage(
            "[StockTicker] This module uses an external API, finnhub.io, which requires an api token to use. " +
                "Go to https://finnhub.io/dashboard and sign up for an account and copy your api token. " +
                "Once you have gotten your api token, you can run this command: " +
                ";set Stocks Token (paste token here)"
        )
        sentwarning = true
    }

    private class StockData(
        val c: Double
    )
}