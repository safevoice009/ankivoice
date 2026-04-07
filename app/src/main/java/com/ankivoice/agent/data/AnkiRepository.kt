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
        
        // We use the Note CONTENT_URI for simple fetching.
        // In a real app, we would use the ReviewInfo.CONTENT_URI to get ONLY due cards.
        // But for this AnkiVoice MVP, we'll fetch cards from the selected deck.
        val projection = arrayOf(
            FlashCardsContract.Note._ID,
            FlashCardsContract.Note.FLDS
        )
        
        // We query the cards table instead of notes for filtering by deckId easily
        val selection = "${FlashCardsContract.Card.DECK_ID} = ?"
        val selectionArgs = arrayOf(deckId.toString())

        try {
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
                    if (noteIdIndex == -1 || questIndex == -1 || ansIndex == -1) {
                        Log.w("AnkiRepository", "Missing column index: nid=$noteIdIndex, q=$questIndex, a=$ansIndex")
                        continue
                    }
                    val noteId = it.getLong(noteIdIndex)
                    val rawQuestion = it.getString(questIndex) ?: ""
                    val rawAnswer = it.getString(ansIndex) ?: ""
                    
                    val question = Html.fromHtml(rawQuestion, Html.FROM_HTML_MODE_LEGACY).toString()
                    val answer = Html.fromHtml(rawAnswer, Html.FROM_HTML_MODE_LEGACY).toString()
                    
                    notes.add(AnkiNote(noteId, question, answer))
                }
            }
        } catch (e: Exception) {
            Log.e("AnkiRepository", "Error fetching cards: ${e.message}")
        }
        
        Log.d("AnkiRepository", "Fetched ${notes.size} cards for deck $deckId")
        
        // Fallback to mock cards if empty
        if (notes.isEmpty()) {
            Log.i("AnkiRepository", "Deck $deckId is empty, providing mock cards fallback")
            notes.add(AnkiNote(-1L, "Welcome to AnkiVoice! This is a sample card. What is the goal of this app?", "To help you study Anki cards hands-free using your voice."))
            notes.add(AnkiNote(-2L, "How do you answer a card in AnkiVoice?", "Just speak your answer after the agent asks the question."))
            notes.add(AnkiNote(-3L, "Does this app support offline study?", "Yes, the AI engines for voice and ear run entirely on your device."))
        }
        
        return notes
    }

    fun answerCard(noteId: Long, ease: Int) {
        try {
            val values = ContentValues().apply {
                put(FlashCardsContract.ReviewInfo.NOTE_ID, noteId)
                put(FlashCardsContract.ReviewInfo.CARD_ORD, 0) // Basic support for card ordinal 0
                put(FlashCardsContract.ReviewInfo.EASE, ease)
                put(FlashCardsContract.ReviewInfo.TIME_TAKEN, 5000L) // Example 5s
            }
            contentResolver.update(FlashCardsContract.ReviewInfo.CONTENT_URI, values, null, null)
        } catch (e: Exception) {
            Log.e("AnkiRepository", "Error answering card: ${e.message}")
        }
    }
}

data class AnkiNote(val id: Long, val front: String, val back: String)
