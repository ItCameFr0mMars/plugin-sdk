import com.google.gson.Gson
import com.lambda.client.util.TickTimer
import com.lambda.client.util.TimeUnit
import com.lambda.client.util.WebUtils
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.plugin.api.PluginLabelHud

internal object StocksHud: PluginLabelHud(
    name = "Stocks",
    category = Category.MISC,
    description = "Live updating stock price",
    pluginMain = PluginStocks
) {
    private var symbol by setting("Symbol", "TSLA")
    private val tickdelay by setting("Delay", 30, 20..120, 1)
    private var token by setting("Token", "Set your token with the command ;set Stocks Token (token)")
    private val ticktimer = TickTimer(TimeUnit.SECONDS)
    private var url = "https://finnhub.io/api/v1/quote?symbol=$symbol&token=$token"
    private var stockData = StockData(0.0)
    private var price = 0.0
    private var sentwarning = false

    override fun SafeClientEvent.updateText() {
        if (sentwarning == false) {
            sendWarning()
        }

        if (ticktimer.tick(tickdelay)) {
            updateStockData()
        }
        displayText.add("Current Price of ${symbol.toUpperCase()} is", primaryColor)
        displayText.add("$price", secondaryColor)
    }

    private fun updateStockData() {
        if (token.length != 20) {

        } else {
            url = "https://finnhub.io/api/v1/quote?symbol=${symbol.toUpperCase()}&token=$token"
            price = Gson().fromJson(WebUtils.getUrlContents(url), StockData::class.java).c
        }
    }


    private fun sendWarning() {
        MessageSendHelper.sendWarningMessage(
            "This module uses an external API, finnhub.io, which requires an api token to use. " +
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