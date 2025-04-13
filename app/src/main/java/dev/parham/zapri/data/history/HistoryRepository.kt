package dev.parham.zapri.data.history

class HistoryRepository {
    private val historyStack = mutableListOf<String>()

    // Push a URL to the history stack
    fun push(url: String) {
        historyStack.add(url)
    }

    // Pop the last URL from the history stack
    fun pop(): String? {
        return if (historyStack.isNotEmpty()) {
            historyStack.removeAt(historyStack.size - 1)
        } else {
            null
        }
    }

    // Check if the history stack is empty
    fun isEmpty(): Boolean {
        return historyStack.isEmpty()
    }
}

