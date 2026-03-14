package com.example.spamguard

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.sqrt
import kotlin.math.exp

class SpamClassifierEngine(private val context: Context) {

    private lateinit var vocabulary: HashMap<String, Int>
    private lateinit var idfValues: DoubleArray

    private lateinit var svmWeights: Array<DoubleArray>
    private lateinit var svmBias: DoubleArray
    private lateinit var classes: Array<String>

    init {
        loadModel()
    }

    private fun loadJsonFromAssets(fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.readText()
        reader.close()
        return content
    }

    private fun loadModel() {

        // Load vocabulary
        val vocabString = loadJsonFromAssets("vocabulary.json")
        val vocabJson = JSONObject(vocabString)

        vocabulary = HashMap()

        val keys = vocabJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            vocabulary[key] = vocabJson.getInt(key)
        }

        // Load IDF values
        val idfString = loadJsonFromAssets("idf_values.json")
        val idfArrayJson = JSONArray(idfString)

        idfValues = DoubleArray(idfArrayJson.length())

        for (i in 0 until idfArrayJson.length()) {
            idfValues[i] = idfArrayJson.getDouble(i)
        }

        // Load SVM parameters
        val svmString = loadJsonFromAssets("svm_params.json")
        val svmJson = JSONObject(svmString)

        val classesJson = svmJson.getJSONArray("classes")
        classes = Array(classesJson.length()) { i ->
            classesJson.getString(i)
        }

        val biasJson = svmJson.getJSONArray("bias")
        svmBias = DoubleArray(biasJson.length()) { i ->
            biasJson.getDouble(i)
        }

        val weightsJson = svmJson.getJSONArray("weights")

        svmWeights = Array(weightsJson.length()) { i ->
            val row = weightsJson.getJSONArray(i)
            DoubleArray(row.length()) { j ->
                row.getDouble(j)
            }
        }
    }

    private fun cleanText(text: String): String {

        var cleaned = text.lowercase()

        cleaned = cleaned.replace(Regex("http\\S+|www\\S+"), " ")
        cleaned = cleaned.replace(Regex("\\d+"), " ")
        cleaned = cleaned.replace(Regex("[^a-z\\s]"), " ")
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        return cleaned
    }

    private fun buildTfVector(cleanedText: String): DoubleArray {

        val tfVector = DoubleArray(vocabulary.size)

        val tokens = cleanedText.split(Regex("\\s+"))

        for (token in tokens) {

            if (token.isEmpty()) continue

            val index = vocabulary[token]

            if (index != null) {
                tfVector[index] += 1.0
            }
        }

        return tfVector
    }

    private fun applyTfIdfAndNormalize(tfVector: DoubleArray): DoubleArray {

        val tfidfVector = DoubleArray(tfVector.size)

        for (i in tfVector.indices) {
            tfidfVector[i] = tfVector[i] * idfValues[i]
        }

        var norm = 0.0

        for (value in tfidfVector) {
            norm += value * value
        }

        norm = sqrt(norm)

        if (norm > 0) {
            for (i in tfidfVector.indices) {
                tfidfVector[i] /= norm
            }
        }

        return tfidfVector
    }

    fun predict(message: String): Pair<String, Int> {

        val cleaned = cleanText(message)

        val tfVector = buildTfVector(cleaned)

        val tfidfVector = applyTfIdfAndNormalize(tfVector)

        var score = svmBias[0]

        for (i in tfidfVector.indices) {
            score += tfidfVector[i] * svmWeights[0][i]
        }

        val probability = 1.0 / (1.0 + exp(-score))
        val confidence = (probability * 100).toInt()

        val label = when {
            score > 1 -> "spam"
            score > 0 -> "potential_spam"
            else -> "ham"
        }

        return Pair(label, confidence)
    }
}
