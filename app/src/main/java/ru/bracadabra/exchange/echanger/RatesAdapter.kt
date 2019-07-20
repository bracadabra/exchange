package ru.bracadabra.exchange.echanger

import com.squareup.moshi.*

class RatesAdapter : JsonAdapter<List<Rate>>() {

    @FromJson
    override fun fromJson(reader: JsonReader): List<Rate> {
        val rates = mutableListOf<Rate>()

        reader.beginObject()
        while (reader.peek() != JsonReader.Token.END_OBJECT) {
            val currency = reader.nextName()
            val rate = reader.nextDouble().toFloat()

            rates.add(Rate(currency, rate))
        }
        reader.endObject()

        return rates
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: List<Rate>?) {
        if (value == null) {
            return
        }

        writer.beginObject()
        value.forEach { rate ->
            writer.name(rate.currency)
            writer.value(rate.rate)
        }
        writer.endObject()
    }
}