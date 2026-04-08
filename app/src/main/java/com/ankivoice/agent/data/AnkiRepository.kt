package com.ankivoice.agent.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.Html
import android.util.Log
import com.ankivoice.agent.ui.screens.Deck
import com.ichi2.anki.FlashCardsContract
import com.ichi2.anki.api.AddContentApi
import com.ichi2.anki.api.Utils

class AnkiRepository(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
    private val api = AddContentApi(context)

    fun getDeckList(): List<Deck> {
        val deckList = mutableListOf<Deck>()
        try {
            val decks: Map<Long, String>? = api.getDeckList()
            decks?.forEach { (id: Long, name: String) ->
                deckList.add(Deck(id, name))
            }
        } catch (e: Exception) {
            Log.e("AnkiRepository", "Error fetching decks: ${e.message}")
        }
        return deckList
    }

    fun getDueCards(deckId: Long): List<AnkiNote> {
        val notes = mutableListOf<AnkiNote>()
        
        // Detailed query for cards in the specific deck
        val selection = "${FlashCardsContract.Card.DECK_ID} = ?"
        val selectionArgs = arrayOf(deckId.toString())

        try {
            android.util.Log.d("AnkiRepository", "Querying cards for deckId: $deckId")
            val cursor: Cursor? = contentResolver.query(
                FlashCardsContract.Card.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                val noteIdIndex = it.getColumnIndex(FlashCardsContract.Card.NOTE_ID)
                val questIndex = it.getColumnIndex(FlashCardsContract.Card.QUESTION)
                val ansIndex = it.getColumnIndex(FlashCardsContract.Card.ANSWER)
                
                while (it.moveToNext()) {
                    if (noteIdIndex == -1 || questIndex == -1 || ansIndex == -1) continue
                    
                    val noteId = it.getLong(noteIdIndex)
                    val rawQuestion = it.getString(questIndex) ?: ""
                    val rawAnswer = it.getString(ansIndex) ?: ""
                    
                    // Clean HTML tags for cleaner voice synthesis
                    val question = Html.fromHtml(rawQuestion, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                    val answer = Html.fromHtml(rawAnswer, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                    
                    if (question.isNotEmpty()) {
                        notes.add(AnkiNote(noteId, question, answer))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AnkiRepository", "Error fetching cards: ${e.message}")
        }
        
        android.util.Log.d("AnkiRepository", "Successfully fetched ${notes.size} notes from AnkiDroid")
        
        // Fallback for empty decks or accessibility issues
        if (notes.isEmpty()) {
            notes.add(AnkiNote(-1L, "Welcome to AnkiVoice Agent!", "This is a sample card. What is the goal of this app?"))
            notes.add(AnkiNote(-2L, "How do you study hands-free?", "Just listen to the agent and speak your answer when prompted."))
        }
        
        return notes
    }

    fun answerCard(noteId: Long, ease: Int) {
        try {
            val values = ContentValues().apply {
                put(FlashCardsContract.ReviewInfo.NOTE_ID, noteId)
                put(FlashCardsContract.ReviewInfo.CARD_ORD, 0)
                put(FlashCardsContract.ReviewInfo.EASE, ease)
                put(FlashCardsContract.ReviewInfo.TIME_TAKEN, 5000L)
            }
            contentResolver.update(FlashCardsContract.ReviewInfo.CONTENT_URI, values, null, null)
            android.util.Log.d("AnkiRepository", "Answered card $noteId with ease $ease")
        } catch (e: Exception) {
            android.util.Log.e("AnkiRepository", "Error answering card: ${e.message}")
        }
    }
}

data class AnkiNote(val id: Long, val front: String, val back: String)
